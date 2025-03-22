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
    private static final String INPUT = "input";
    private static final String OFFER = "offer";
    private static final String COND = "$cond";
    private static final String HAS_AVAILABLE_OFFERS = "hasAvailableOffers";
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

        // Convert ObjectId to string for easier handling
        operations.add(Aggregation.addFields()
                .addField(STRING_ID)
                .withValue(ConvertOperators.ToString.toString("$" + ID))
                .build());

        // Basic filtering (category, name, etc.)
        operations.add(Aggregation.match(createBaseCriteria()));

        // Sort priceHistory inside each offer by timestamp desc BEFORE unwinding
        // ensures the most recent price will be first after unwinding
        operations.add(context -> new Document(
                "$addFields",
                new Document(
                        OFFERS,
                        new Document(
                                "$map",
                                new Document(INPUT, "$" + OFFERS)
                                        .append("as", OFFER)
                                        .append(
                                                "in",
                                                new Document(
                                                        "$mergeObjects",
                                                        Arrays.asList(
                                                                "$$offer",
                                                                new Document(
                                                                        "priceHistory",
                                                                        new Document(
                                                                                "$sortArray",
                                                                                new Document(
                                                                                                INPUT,
                                                                                                "$$offer.priceHistory")
                                                                                        .append(
                                                                                                "sortBy",
                                                                                                new Document(
                                                                                                        TIMESTAMP,
                                                                                                        -1)))))))))));

        // Unwinding offers
        operations.add(Aggregation.unwind(OFFERS, true));

        // Create temporary fields for shop and its name
        operations.add(Aggregation.addFields()
                .addField(SHOP_OBJECT)
                .withValue(OFFERS_SHOP)
                .addField("shopHumanName")
                .withValue(OFFERS_SHOP)
                .build());

        // Unwinding price history - Now we'll get the FIRST item which is guaranteed to be the most recent
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        // Group to ensure we only keep one price history item per shop (the most recent one)
        operations.add(context -> new Document(
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
                        .append(TIMESTAMP, new Document(FIRST, "$offers.priceHistory.timestamp"))));

        return operations;
    }

    public List<AggregationOperation> getPriceFilterOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

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
                .push(new Document(SHOP_OBJECT, "$" + SHOP_OBJECT)
                        .append(SHOP_NAME, "$" + SHOP_NAME)
                        .append(PRICE, "$" + PRICE)
                        .append(AVAILABLE_PRICE, "$" + AVAILABLE_PRICE)
                        .append(CURRENCY, "$" + CURRENCY)
                        .append(TIMESTAMP, "$" + TIMESTAMP)
                        .append(IS_AVAILABLE, "$" + IS_AVAILABLE))
                .as(OFFERS);

        operations.add(groupOperation);

        // Count available offers and calculate lowest prices
        operations.add(Aggregation.addFields()
                .addField(OFFER_COUNT)
                .withValue(ArrayOperators.Size.lengthOfArray(ArrayOperators.Filter.filter("$" + OFFERS)
                        .as(OFFER)
                        .by(ComparisonOperators.Eq.valueOf("$$offer." + IS_AVAILABLE)
                                .equalToValue(true))))
                .build());

        // Add fields for lowest offer and availability
        operations.add(Aggregation.addFields()
                .addField(HAS_AVAILABLE_OFFERS)
                .withValue(ComparisonOperators.Gt.valueOf("$" + OFFER_COUNT).greaterThanValue(0))
                .build());

        // Use a direct MongoDB document approach for the lowest offer logic to avoid null issues
        operations.add(context -> new Document(
                "$addFields",
                new Document(
                                LOWEST_OFFER,
                                new Document(
                                        COND,
                                        new Document("if", new Document("$gt", Arrays.asList("$" + OFFER_COUNT, 0)))
                                                .append(
                                                        "then",
                                                        new Document(
                                                                "$arrayElemAt",
                                                                Arrays.asList(
                                                                        new Document(
                                                                                "$sortArray",
                                                                                new Document(
                                                                                                INPUT,
                                                                                                new Document(
                                                                                                        "$filter",
                                                                                                        new Document(
                                                                                                                        INPUT,
                                                                                                                        "$"
                                                                                                                                + OFFERS)
                                                                                                                .append(
                                                                                                                        "as",
                                                                                                                        OFFER)
                                                                                                                .append(
                                                                                                                        "cond",
                                                                                                                        new Document(
                                                                                                                                "$eq",
                                                                                                                                Arrays
                                                                                                                                        .asList(
                                                                                                                                                "$$offer."
                                                                                                                                                        + IS_AVAILABLE,
                                                                                                                                                true)))))
                                                                                        .append(
                                                                                                "sortBy",
                                                                                                new Document(
                                                                                                        PRICE, 1))),
                                                                        0)))
                                                .append("else", new Document()) // Empty document instead of null
                                        ))
                        .append("mainImageUrl", new Document("$arrayElemAt", Arrays.asList("$" + IMAGES_FIELD, 0)))));

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
                new Document("id", "$" + STRING_ID)
                        .append(NAME, 1)
                        .append("ean", 1)
                        .append(CATEGORY, 1)
                        .append("mainImageUrl", 1)
                        .append(OFFER_COUNT, 1)
                        .append(
                                PRICE_FIELD,
                                new Document(
                                        COND,
                                        new Document("if", "$" + HAS_AVAILABLE_OFFERS)
                                                .append("then", "$" + LOWEST_OFFER + "." + PRICE)
                                                .append("else", null)))
                        .append(
                                "lowestPriceCurrency",
                                new Document(
                                        COND,
                                        new Document("if", "$" + HAS_AVAILABLE_OFFERS)
                                                .append("then", "$" + LOWEST_OFFER + "." + CURRENCY)
                                                .append("else", null)))
                        .append(
                                "lowestPriceShop",
                                new Document(
                                        COND,
                                        new Document("if", "$" + HAS_AVAILABLE_OFFERS)
                                                .append("then", "$" + LOWEST_OFFER + "." + SHOP_NAME)
                                                .append("else", null)))
                        .append(IS_AVAILABLE, "$" + HAS_AVAILABLE_OFFERS));
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
                        sortDoc.append("lowestOffer.price", direction);
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
