package it.compare.backend.product.aggregation;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

@Getter
@Builder
public class ProductAggregationBuilder {
    // Field constants to avoid duplication
    public static final String PRICE_FIELD = "lowestCurrentPrice";
    public static final String SHOP_OBJECT = "shopObject";
    public static final String STRING_ID = "stringId";
    public static final String OFFERS = "offers";
    public static final String OFFER_PREFIX = "$$offer.";
    public static final String OFFERS_SHOP_FIELD = "offers.shop";
    public static final String OFFERS_SHOP = "$offers.shop";
    public static final String IS_AVAILABLE = "isAvailable";
    public static final String CATEGORY = "category";
    public static final String IMAGES_FIELD = "images";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String SHOP_NAME = "shopName";
    public static final String PRICE = "price";
    public static final String AVAILABLE_PRICE = "availablePrice";
    public static final String CURRENCY = "currency";
    public static final String TIMESTAMP = "timestamp";
    public static final String LOWEST_OFFER = "lowestOffer";
    public static final String OFFER_COUNT = "offerCount";
    public static final String INPUT = "input";
    public static final String OFFER = "offer";
    public static final String COND = "$cond";
    public static final String HAS_AVAILABLE_OFFERS = "hasAvailableOffers";
    public static final String ADD_FIELDS = "$addFields";
    public static final String MAP = "$map";
    public static final String FILTER = "$filter";
    public static final String SORT_ARRAY = "$sortArray";
    public static final String MERGE_OBJECTS = "$mergeObjects";
    public static final String PRICE_HISTORY = "priceHistory";
    public static final int AVAILABILITY_DAYS_THRESHOLD = 3;

    // Search parameters
    private String searchName;
    private String searchCategory;
    private List<String> shop;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isAvailable;
    private Pageable pageable;

    /**
     * Creates a complete search aggregation pipeline
     */
    public Aggregation buildSearchAggregation() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Add all aggregation operations
        operations.addAll(getBaseAggregationOperations());
        operations.addAll(getPriceFilterOperations());
        operations.addAll(getGroupingOperations());
        operations.addAll(getSortingAndPaginationOperations());
        operations.add(getProjectionOperation());

        return Aggregation.newAggregation(operations);
    }

    /**
     * Creates a count aggregation pipeline for pagination
     */
    public Aggregation buildCountAggregation() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Add base operations (shared with search)
        operations.addAll(getBaseAggregationOperations());

        // Add price and availability filters (shared with search)
        operations.addAll(getPriceFilterOperations());

        // Group by product ID to count unique products
        operations.add(Aggregation.group("$_id.productId").count().as("count"));

        // Final count operation
        operations.add(Aggregation.group().count().as("count"));

        return Aggregation.newAggregation(operations);
    }

    /**
     * Creates base operations for both search and count
     */
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
        operations.add(context -> new Document(
                ADD_FIELDS,
                new Document(
                        OFFERS,
                        new Document(
                                MAP,
                                new Document(INPUT, "$" + OFFERS)
                                        .append("as", OFFER)
                                        .append(
                                                "in",
                                                new Document(
                                                        MERGE_OBJECTS,
                                                        Arrays.asList(
                                                                "$$offer",
                                                                new Document(
                                                                        PRICE_HISTORY,
                                                                        new Document(
                                                                                SORT_ARRAY,
                                                                                new Document(
                                                                                                INPUT,
                                                                                                OFFER_PREFIX
                                                                                                        + PRICE_HISTORY)
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

        // Unwinding price history
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        // Group to ensure we only keep one price history item per shop (the most recent one)
        operations.add(
                Aggregation.group(Fields.from(Fields.field("productId", "$" + ID), Fields.field("shop", OFFERS_SHOP)))
                        .first("$" + STRING_ID)
                        .as(STRING_ID)
                        .first("$" + NAME)
                        .as(NAME)
                        .first("$ean")
                        .as("ean")
                        .first("$" + CATEGORY)
                        .as(CATEGORY)
                        .first("$" + IMAGES_FIELD)
                        .as(IMAGES_FIELD)
                        .first("$" + SHOP_OBJECT)
                        .as(SHOP_OBJECT)
                        .first("$shopHumanName")
                        .as(SHOP_NAME)
                        .first("$offers.priceHistory.price")
                        .as(PRICE)
                        .first("$offers.priceHistory.currency")
                        .as(CURRENCY)
                        .first("$offers.priceHistory.timestamp")
                        .as(TIMESTAMP));

        return operations;
    }

    /**
     * Creates operations for price filtering and availability
     */
    public List<AggregationOperation> getPriceFilterOperations() {
        List<AggregationOperation> operations = new ArrayList<>();

        // Calculate availability based on timestamp directly in the database
        operations.add(Aggregation.addFields()
                .addField(IS_AVAILABLE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Gte.valueOf("$" + TIMESTAMP)
                                .greaterThanEqualTo(
                                        DateOperators.DateSubtract.subtractValue(AVAILABILITY_DAYS_THRESHOLD, "day")
                                                .fromDate(SystemVariable.NOW)))
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

    /**
     * Creates operations for grouping products and finding the lowest prices
     */
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

        // Count available offers and calculate the lowest prices
        operations.add(Aggregation.addFields()
                .addField(OFFER_COUNT)
                .withValue(ArrayOperators.Size.lengthOfArray(ArrayOperators.Filter.filter("$" + OFFERS)
                        .as(OFFER)
                        .by(ComparisonOperators.Eq.valueOf(OFFER_PREFIX + IS_AVAILABLE)
                                .equalToValue(true))))
                .build());

        // Add fields for lowest offer and availability
        operations.add(Aggregation.addFields()
                .addField(HAS_AVAILABLE_OFFERS)
                .withValue(ComparisonOperators.Gt.valueOf("$" + OFFER_COUNT).greaterThanValue(0))
                .build());

        // Use a direct MongoDB document approach for the lowest offer logic to avoid null issues
        operations.add(context -> new Document(
                ADD_FIELDS,
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
                                                                                SORT_ARRAY,
                                                                                new Document(
                                                                                                INPUT,
                                                                                                new Document(
                                                                                                        FILTER,
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
                                                                                                                                                OFFER_PREFIX
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

    /**
     * Creates operations for sorting and pagination
     */
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

    /**
     * Creates the final projection operation
     */
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

    /**
     * Creates a price range filter operation
     */
    private AggregationOperation createPriceRangeFilterOperation() {
        // Make sure we have valid values
        var minPriceValue = minPrice != null ? minPrice.doubleValue() : null;
        var maxPriceValue = maxPrice != null ? maxPrice.doubleValue() : null;

        var priceCriteria = Criteria.where(PRICE);

        if (minPriceValue != null) {
            priceCriteria = priceCriteria.gte(minPriceValue);
        }

        if (maxPriceValue != null) {
            priceCriteria = priceCriteria.lte(maxPriceValue);
        }

        return Aggregation.match(priceCriteria);
    }

    /**
     * Creates a sort operation based on pageable
     */
    private AggregationOperation createSortOperation() {
        return context -> {
            Document sortDoc = new Document();

            for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                int direction = order.getDirection() == org.springframework.data.domain.Sort.Direction.ASC ? 1 : -1;
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
