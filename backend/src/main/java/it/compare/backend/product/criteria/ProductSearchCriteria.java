package it.compare.backend.product.criteria;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.*;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {
    // Field constants to avoid duplication
    private static final String PRICE_FIELD = "lowestCurrentPrice";
    private static final String SHOP_OBJECT = "shopObject";
    private static final String STRING_ID = "stringId";
    private static final String OFFERS = "offers";
    private static final String OFFERS_SHOP_FIELD = "offers.shop";
    private static final String OFFERS_SHOP = "$offers.shop";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String CATEGORY = "category";
    private static final String FIRST = "$first";
    private static final String ADD_FIELDS = "$addFields";
    private static final String IMAGES_FIELD = "images";
    private static final String MATCH = "$match";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String SHOP_NAME = "shopName";
    private static final String PRICE = "price";
    private static final String CURRENCY = "currency";
    private static final String LOWEST_OFFER = "lowestOffer";
    private static final String LOWEST_PRICE = "lowestPrice";

    private String searchName;
    private String searchCategory;
    private List<String> shop;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Pageable pageable;

    public Aggregation toAggregation() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Add all aggregation operations
        operations.addAll(getBaseAggregationOperations());
        operations.addAll(getPriceFilterOperations());
        operations.addAll(getGroupingOperations());
        operations.addAll(getSortingAndPaginationOperations());
        operations.add(getProjectionOperation());

        return Aggregation.newAggregation(operations);
    }

    public List<AggregationOperation> getBaseAggregationOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Extract ID as string
        operations.add(
                context -> new Document(ADD_FIELDS, new Document(STRING_ID, new Document("$toString", "$" + ID))));

        // Basic filtering (category, name, etc.)
        operations.add(Aggregation.match(createBaseCriteria()));

        // Unwinding offers
        operations.add(Aggregation.unwind(OFFERS, true));

        // Create temporary fields for shop and its name
        operations.add(context ->
                new Document(ADD_FIELDS, new Document(SHOP_OBJECT, OFFERS_SHOP).append("shopHumanName", OFFERS_SHOP)));

        // Sort offers by time to find the latest prices
        operations.add(context -> new Document(
                "$sort", new Document(ID, 1).append(OFFERS_SHOP_FIELD, 1).append("offers.priceHistory.timestamp", -1)));

        // Unwinding price history
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        return operations;
    }

    public List<AggregationOperation> getPriceFilterOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Group by product and shop to find the latest prices for each offer
        operations.add(context -> createGroupByProductAndShopStage());

        // Filter unavailable offers
        operations.add(Aggregation.match(Criteria.where(IS_AVAILABLE).is(true)));

        // Add price range filter if applicable
        if (minPrice != null || maxPrice != null) {
            operations.add(createPriceRangeFilterOperation());
        }

        return operations;
    }

    public List<AggregationOperation> getGroupingOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Group by product to find the lowest price
        operations.add(context -> createGroupByProductStage());

        // Find the offer with the lowest price
        operations.add(context -> createLowestPriceFieldsStage());

        return operations;
    }

    public List<AggregationOperation> getSortingAndPaginationOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Add sorting if applicable
        if (pageable != null && pageable.getSort().isSorted()) {
            operations.add(createSortOperation());
        }

        // Add pagination if applicable
        if (pageable != null) {
            operations.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
            operations.add(Aggregation.limit(pageable.getPageSize()));
        }

        return operations;
    }

    public AggregationOperation getProjectionOperation() {
        return context -> new Document(
                "$project",
                new Document()
                        .append("id", "$" + STRING_ID)
                        .append(NAME, 1)
                        .append("ean", 1)
                        .append(CATEGORY, 1)
                        .append("mainImageUrl", 1)
                        .append(PRICE_FIELD, "$" + LOWEST_OFFER + "." + PRICE)
                        .append("lowestPriceCurrency", "$" + LOWEST_OFFER + "." + CURRENCY)
                        .append("lowestPriceShop", "$" + LOWEST_OFFER + "." + SHOP_NAME)
                        .append("offerCount", 1)
                        .append(IS_AVAILABLE, "$" + LOWEST_OFFER + "." + IS_AVAILABLE));
    }

    private Document createGroupByProductAndShopStage() {
        return new Document(
                "$group",
                new Document(ID, new Document("productId", "$" + ID).append("shop", OFFERS_SHOP))
                        .append(STRING_ID, new Document(FIRST, "$" + STRING_ID))
                        .append(NAME, new Document(FIRST, "$" + NAME))
                        .append("ean", new Document(FIRST, "$ean"))
                        .append(CATEGORY, new Document(FIRST, "$" + CATEGORY))
                        .append(IMAGES_FIELD, new Document(FIRST, "$" + IMAGES_FIELD))
                        .append(SHOP_OBJECT, new Document(FIRST, "$" + SHOP_OBJECT))
                        .append(SHOP_NAME, new Document(FIRST, "$shopHumanName"))
                        .append(PRICE, new Document(FIRST, "$offers.priceHistory.price"))
                        .append(CURRENCY, new Document(FIRST, "$offers.priceHistory.currency"))
                        .append(IS_AVAILABLE, new Document(FIRST, "$offers.priceHistory.isAvailable")));
    }

    private Document createGroupByProductStage() {
        return new Document(
                "$group",
                new Document(ID, "$_id.productId")
                        .append(STRING_ID, new Document(FIRST, "$" + STRING_ID))
                        .append(NAME, new Document(FIRST, "$" + NAME))
                        .append("ean", new Document(FIRST, "$ean"))
                        .append(CATEGORY, new Document(FIRST, "$" + CATEGORY))
                        .append(IMAGES_FIELD, new Document(FIRST, "$" + IMAGES_FIELD))
                        .append(LOWEST_PRICE, new Document("$min", "$" + PRICE))
                        .append("offerCount", new Document("$sum", 1))
                        .append(
                                OFFERS,
                                new Document(
                                        "$push",
                                        new Document(SHOP_OBJECT, "$" + SHOP_OBJECT)
                                                .append(SHOP_NAME, "$" + SHOP_NAME)
                                                .append(PRICE, "$" + PRICE)
                                                .append(CURRENCY, "$" + CURRENCY)
                                                .append(IS_AVAILABLE, "$" + IS_AVAILABLE))));
    }

    private Document createLowestPriceFieldsStage() {
        return new Document(
                ADD_FIELDS,
                new Document(
                                LOWEST_OFFER,
                                new Document(
                                        "$reduce",
                                        new Document("input", "$" + OFFERS)
                                                .append("initialValue", null)
                                                .append(
                                                        "in",
                                                        new Document(
                                                                "$cond",
                                                                Arrays.asList(
                                                                        new Document(
                                                                                "$or",
                                                                                Arrays.asList(
                                                                                        new Document(
                                                                                                "$eq",
                                                                                                Arrays.asList(
                                                                                                        "$$value",
                                                                                                        null)),
                                                                                        new Document(
                                                                                                "$lt",
                                                                                                Arrays.asList(
                                                                                                        "$$this.price",
                                                                                                        "$$value.price")))),
                                                                        "$$this",
                                                                        "$$value")))))
                        .append("mainImageUrl", new Document("$arrayElemAt", Arrays.asList("$" + IMAGES_FIELD, 0))));
    }

    private AggregationOperation createPriceRangeFilterOperation() {
        return context -> {
            Document matchDoc = new Document(MATCH, new Document());

            if (minPrice != null) {
                matchDoc.get(MATCH, Document.class).append(PRICE, new Document("$gte", minPrice));
            }

            if (maxPrice != null) {
                Document priceDoc = matchDoc.get(MATCH, Document.class).get(PRICE, Document.class);
                if (priceDoc == null) {
                    matchDoc.get(MATCH, Document.class).append(PRICE, new Document("$lte", maxPrice));
                } else {
                    priceDoc.append("$lte", maxPrice);
                }
            }

            return matchDoc;
        };
    }

    private AggregationOperation createSortOperation() {
        return context -> {
            Document sortDoc = new Document();

            for (Sort.Order order : pageable.getSort()) {
                int direction = order.getDirection() == Sort.Direction.ASC ? 1 : -1;
                String field = order.getProperty();

                if (field.equals(PRICE_FIELD)) {
                    sortDoc.append(LOWEST_PRICE, direction);
                } else {
                    sortDoc.append(field, direction);
                }
            }

            return new Document("$sort", sortDoc);
        };
    }

    /**
     * Creates base filtering criteria
     */
    public Criteria createBaseCriteria() {
        Criteria criteria = new Criteria();

        if (searchName != null && !searchName.isEmpty()) {
            criteria.and(NAME).regex(searchName, "i");
        }

        if (searchCategory != null && !searchCategory.isEmpty()) {
            var matchingCategory = Arrays.stream(Category.values())
                    .filter(c -> c.getHumanReadableName().equalsIgnoreCase(searchCategory))
                    .findFirst()
                    .orElse(null);

            criteria.and(CATEGORY).is(matchingCategory != null ? matchingCategory : "NON_EXISTENT_CATEGORY");
        }

        if (shop != null && !shop.isEmpty()) {
            var matchingShops = Arrays.stream(Shop.values())
                    .filter(s -> shop.stream()
                            .anyMatch(singleShop -> s.getHumanReadableName().equalsIgnoreCase(singleShop)))
                    .toList();

            if (!matchingShops.isEmpty()) {
                criteria.and(OFFERS_SHOP_FIELD).in(matchingShops);
            } else {
                criteria.and(OFFERS_SHOP_FIELD).in(List.of("NON_EXISTENT_SHOP"));
            }
        }

        return criteria;
    }
}
