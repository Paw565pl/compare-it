package it.compare.backend.product.criteria;

import static org.springframework.data.mongodb.core.aggregation.SystemVariable.NOW;

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
    private static final String IMAGES_FIELD = "images";
    private static final String MATCH = "$match";
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String SHOP_NAME = "shopName";
    private static final String PRICE = "price";
    private static final String AVAILABLE_PRICE = "availablePrice";
    private static final String CURRENCY = "currency";
    private static final String TIMESTAMP = "timestamp";
    private static final String LOWEST_OFFER = "lowestOffer";
    private static final String LOWEST_PRICE = "lowestPrice";
    private static final String LOWEST_AVAILABLE_PRICE = "lowestAvailablePrice";
    private static final String OFFER_COUNT = "offerCount";
    private static final int AVAILABILITY_DAYS_THRESHOLD = 3;

    private String searchName;
    private String searchCategory;
    private List<String> shop;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isAvailable;
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

        operations.add(Aggregation.addFields()
                .addField(STRING_ID)
                .withValue(ConvertOperators.ToString.toString("$" + ID))
                .build());

        // Basic filtering (category, name, etc.)
        operations.add(Aggregation.match(createBaseCriteria()));

        // Unwinding offers
        operations.add(Aggregation.unwind(OFFERS, true));

        // Create temporary fields for shop and its name
        operations.add(Aggregation.addFields()
                .addField(SHOP_OBJECT)
                .withValue(OFFERS_SHOP)
                .addField("shopHumanName")
                .withValue(OFFERS_SHOP)
                .build());

        // Sort offers by time to find the latest prices
        operations.add(Aggregation.sort(Sort.by(ID)
                .ascending()
                .and(Sort.by(OFFERS_SHOP_FIELD).ascending())
                .and(Sort.by("offers.priceHistory.timestamp").descending())));

        // Unwinding price history
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        return operations;
    }

    public List<AggregationOperation> getPriceFilterOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Group by product and shop to find the latest prices for each offer
        operations.add(context -> createGroupByProductAndShopStage());

        // Calculate availability based on timestamp directly in the database
        operations.add(Aggregation.addFields()
                .addField(IS_AVAILABLE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Gte.valueOf("$" + TIMESTAMP)
                                .greaterThanEqualTo(
                                        DateOperators.DateSubtract.subtractValue(AVAILABILITY_DAYS_THRESHOLD, "day")
                                                .fromDate(NOW)))
                        .then(true)
                        .otherwise(false))
                .build());

        // Add a computed field for available price (only valid if isAvailable is true)
        operations.add(Aggregation.addFields()
                .addField(AVAILABLE_PRICE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("$" + IS_AVAILABLE)
                                .equalToValue(true))
                        .then("$" + PRICE)
                        .otherwise(new Document()))
                .build());

        // Filter by availability if requested
        if (isAvailable != null) {
            operations.add(Aggregation.match(Criteria.where(IS_AVAILABLE).is(isAvailable)));
        }

        // Add price range filter if requested
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
        return Aggregation.project()
                .andExpression("$" + STRING_ID)
                .as("id")
                .andInclude(NAME, "ean", CATEGORY, "mainImageUrl", OFFER_COUNT)
                .andExpression("$" + LOWEST_OFFER + "." + PRICE)
                .as(PRICE_FIELD)
                .andExpression("$" + LOWEST_OFFER + "." + CURRENCY)
                .as("lowestPriceCurrency")
                .andExpression("$" + LOWEST_OFFER + "." + SHOP_NAME)
                .as("lowestPriceShop")
                .andExpression("$hasAvailableOffers")
                .as(IS_AVAILABLE);
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
                        .append(TIMESTAMP, new Document(FIRST, "$offers.priceHistory.timestamp")));
    }

    private Document createGroupByProductStage() {

        var groupOperation = Aggregation.group("$_id.productId")
                .first(STRING_ID)
                .as(STRING_ID)
                .first(NAME)
                .as(NAME)
                .first("ean")
                .as("ean")
                .first(CATEGORY)
                .as(CATEGORY)
                .first(IMAGES_FIELD)
                .as(IMAGES_FIELD)
                .min(PRICE)
                .as(LOWEST_PRICE)
                .min(AVAILABLE_PRICE)
                .as(LOWEST_AVAILABLE_PRICE)
                .sum(ConditionalOperators.Cond.when(ComparisonOperators.Eq.valueOf("$" + IS_AVAILABLE)
                                .equalToValue(true))
                        .then(1)
                        .otherwise(0))
                .as(OFFER_COUNT)
                .push(new Document(SHOP_OBJECT, "$" + SHOP_OBJECT)
                        .append(SHOP_NAME, "$" + SHOP_NAME)
                        .append(PRICE, "$" + PRICE)
                        .append(AVAILABLE_PRICE, "$" + AVAILABLE_PRICE)
                        .append(CURRENCY, "$" + CURRENCY)
                        .append(TIMESTAMP, "$" + TIMESTAMP)
                        .append(IS_AVAILABLE, "$" + IS_AVAILABLE))
                .as(OFFERS);

        return groupOperation.toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    private Document createLowestPriceFieldsStage() {
        AddFieldsOperation addFieldsOperation = Aggregation.addFields()
                .addField(LOWEST_OFFER)
                .withValue(ArrayOperators.Reduce.arrayOf("$" + OFFERS)
                        .withInitialValue(new Document())
                        .reduce(ConditionalOperators.when(BooleanOperators.Or.or(
                                        ComparisonOperators.Eq.valueOf("$$value")
                                                .equalToValue(new Document()),
                                        BooleanOperators.And.and(
                                                ComparisonOperators.Eq.valueOf("$$this.isAvailable")
                                                        .equalToValue(true),
                                                BooleanOperators.Or.or(
                                                        ComparisonOperators.Ne.valueOf("$$value.isAvailable")
                                                                .notEqualToValue(true),
                                                        ComparisonOperators.Lt.valueOf("$$this.price")
                                                                .lessThan("$$value.price")))))
                                .then("$$this")
                                .otherwise("$$value")))
                .addField("hasAvailableOffers")
                .withValue(ComparisonOperators.Gt.valueOf(
                                ArrayOperators.Size.lengthOfArray(ArrayOperators.Filter.filter("$offers")
                                        .as("offer")
                                        .by(ComparisonOperators.Eq.valueOf("$$offer.isAvailable")
                                                .equalToValue(true))))
                        .greaterThanValue(0))
                .addField("mainImageUrl")
                .withValue(
                        ArrayOperators.ArrayElemAt.arrayOf("$" + IMAGES_FIELD).elementAt(0))
                .build();

        return addFieldsOperation.toDocument(Aggregation.DEFAULT_CONTEXT);
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

                // Map field names to their internal representation
                switch (field) {
                    case PRICE_FIELD:
                        sortDoc.append(LOWEST_AVAILABLE_PRICE, direction);
                        break;
                    case OFFER_COUNT:
                        sortDoc.append(OFFER_COUNT, direction);
                        break;
                    default:
                        sortDoc.append(field, direction);
                        break;
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
