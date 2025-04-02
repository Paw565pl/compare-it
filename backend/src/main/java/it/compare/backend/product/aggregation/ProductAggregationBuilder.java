package it.compare.backend.product.aggregation;

import static it.compare.backend.product.service.ProductService.AVAILABILITY_DAYS_THRESHOLD;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.bson.Document;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
    public static final String OFFERS_COUNT = "offersCount";
    public static final String INPUT = "input";
    public static final String OFFER = "offer";
    public static final String COND = "$cond";
    public static final String HAS_AVAILABLE_OFFERS = "hasAvailableOffers";
    public static final String ADD_FIELDS = "$addFields";
    public static final String MAP = "$map";
    public static final String SORT_ARRAY = "$sortArray";
    public static final String MERGE_OBJECTS = "$mergeObjects";
    public static final String PRICE_HISTORY = "priceHistory";

    // Search parameters
    private final String searchName;
    private final String searchCategory;
    private final List<String> shop;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Boolean isAvailable;
    private final Pageable pageable;

    public TypedAggregation<Product> buildSearchAggregation() {
        var operations = createCommonAggregationOperations();

        operations.addAll(createSortingAndPaginationOperations());
        operations.add(createProjectionOperation());

        return Aggregation.newAggregation(Product.class, operations);
    }

    public TypedAggregation<Product> buildCountAggregation() {
        var operations = createCommonAggregationOperations();

        operations.add(Aggregation.group("$_id").count().as("count"));
        operations.add(Aggregation.group().count().as("count"));

        return Aggregation.newAggregation(Product.class, operations);
    }

    /**
     * Creates common operations for both search and count aggregations
     */
    private List<AggregationOperation> createCommonAggregationOperations() {
        var operations = new ArrayList<AggregationOperation>();

        operations.addAll(createBaseAggregationOperations());
        operations.addAll(createPriceFilterOperations());
        operations.addAll(createGroupingOperations());

        if (isAvailable != null) {
            operations.add(
                    Aggregation.match(Criteria.where(HAS_AVAILABLE_OFFERS).is(isAvailable)));
        }

        return operations;
    }

    public List<AggregationOperation> createBaseAggregationOperations() {
        var operations = new ArrayList<AggregationOperation>();
        operations.add(Aggregation.match(createBaseCriteria()));

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

        operations.add(Aggregation.unwind(OFFERS, true));

        operations.add(Aggregation.addFields()
                .addField(SHOP_OBJECT)
                .withValue(OFFERS_SHOP)
                .addField("shopHumanName")
                .withValue(OFFERS_SHOP)
                .build());

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

    public List<AggregationOperation> createPriceFilterOperations() {
        var operations = new ArrayList<AggregationOperation>();

        operations.add(Aggregation.addFields()
                .addField(IS_AVAILABLE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Gte.valueOf("$" + TIMESTAMP)
                                .greaterThanEqualTo(
                                        DateOperators.DateSubtract.subtractValue(AVAILABILITY_DAYS_THRESHOLD, "day")
                                                .fromDate(SystemVariable.NOW)))
                        .then(true)
                        .otherwise(false))
                .build());

        operations.add(Aggregation.addFields()
                .addField(AVAILABLE_PRICE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("$" + IS_AVAILABLE)
                                .equalToValue(true))
                        .then("$" + PRICE)
                        .otherwise(new Document()))
                .build());

        if (minPrice != null || maxPrice != null) {
            operations.add(createPriceRangeFilterOperation());
        }

        return operations;
    }

    /**
     * Creates operations for grouping products and finding the lowest prices
     */
    public List<AggregationOperation> createGroupingOperations() {
        var operations = new ArrayList<AggregationOperation>();

        operations.add(Aggregation.group("$_id.productId")
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
                .as(OFFERS));

        operations.add(Aggregation.addFields()
                .addField(OFFERS_COUNT)
                .withValue(ArrayOperators.Size.lengthOfArray(ArrayOperators.Filter.filter("$" + OFFERS)
                        .as(OFFER)
                        .by(ComparisonOperators.Eq.valueOf(OFFER_PREFIX + IS_AVAILABLE)
                                .equalToValue(true))))
                .build());

        operations.add(Aggregation.addFields()
                .addField(HAS_AVAILABLE_OFFERS)
                .withValue(ComparisonOperators.Gt.valueOf("$" + OFFERS_COUNT).greaterThanValue(0))
                .build());

        operations.add(Aggregation.addFields()
                .addField(LOWEST_OFFER)
                .withValueOf(ConditionalOperators.when(ComparisonOperators.Gt.valueOf("$" + OFFERS_COUNT)
                                .greaterThanValue(0))
                        .then(ArrayOperators.ArrayElemAt.arrayOf(
                                        ArrayOperators.SortArray.sortArrayOf(ArrayOperators.Filter.filter("$" + OFFERS)
                                                        .as(OFFER)
                                                        .by(ComparisonOperators.Eq.valueOf(OFFER_PREFIX + IS_AVAILABLE)
                                                                .equalToValue(true)))
                                                .by(Sort.by(Sort.Order.asc(PRICE), Sort.Order.desc(TIMESTAMP))))
                                .elementAt(0))
                        .otherwise(new Document()))
                .build());

        operations.add(Aggregation.addFields()
                .addField("mainImageUrl")
                .withValueOf(
                        ArrayOperators.ArrayElemAt.arrayOf("$" + IMAGES_FIELD).elementAt(0))
                .build());

        return operations;
    }

    public List<AggregationOperation> createSortingAndPaginationOperations() {
        var operations = new ArrayList<AggregationOperation>();

        operations.add(createSortOperation());

        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        return operations;
    }

    public AggregationOperation createProjectionOperation() {
        return context -> new Document(
                "$project",
                new Document("_id", 1)
                        .append(NAME, 1)
                        .append("ean", 1)
                        .append(CATEGORY, 1)
                        .append("mainImageUrl", 1)
                        .append(OFFERS_COUNT, 1)
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
                                                .append("then", "$" + LOWEST_OFFER + "." + SHOP_OBJECT)
                                                .append("else", null)))
                        .append(IS_AVAILABLE, "$" + HAS_AVAILABLE_OFFERS));
    }

    private AggregationOperation createPriceRangeFilterOperation() {
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

    private AggregationOperation createSortOperation() {
        return context -> {
            var sortDoc = new Document();

            for (var order : pageable.getSort()) {
                var direction = order.getDirection() == Direction.ASC ? 1 : -1;
                var field = order.getProperty();

                if (field.equalsIgnoreCase(PRICE_FIELD)) sortDoc.append("lowestOffer.price", direction);
                else sortDoc.append(field, direction);
            }

            sortDoc.append("_id", 1);
            return new Document("$sort", sortDoc);
        };
    }

    public Criteria createBaseCriteria() {
        var criteria = new Criteria();

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
