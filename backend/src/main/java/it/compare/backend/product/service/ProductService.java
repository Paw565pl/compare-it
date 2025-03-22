package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    // Constants to avoid duplication
    private static final String OFFERS_SHOP = "$offers.shop";
    private static final String ADD_FIELDS = "$addFields";
    private static final String PRICE = "price";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final String DATE_FROM_PARTS = "$dateFromParts";
    private static final String PRICE_TIMESTAMP = "$$price.timestamp";
    private static final String NOW = "$$NOW";
    private static final String DAY = "day";
    private static final String TIMESTAMP = "timestamp";
    private static final String OFFERS = "offers";
    private static final String COND = "$cond";
    private static final String INPUT = "input";
    private static final String OFFERS_PRICE_HISTORY_TIMESTAMP = "offers.priceHistory.timestamp";
    private static final String MAP = "$map";
    private static final String FILTER = "$filter";
    private static final String SORT_ARRAY = "$sortArray";
    private static final String MERGE_OBJECTS = "$mergeObjects";
    private static final int AVAILABILITY_DAYS_THRESHOLD = 3;
    private static final int MAX_PRICE_STAMP_RANGE_DAYS = 180;
    private static final int MIN_PRICE_STAMP_RANGE_DAYS = 1;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponse> findAll(ProductFiltersDto filters, Pageable pageable) {
        // Create search criteria from filters
        var criteria = createSearchCriteria(filters, pageable);

        // Execute main aggregation
        AggregationResults<ProductListResponse> results = executeMainAggregation(criteria);
        List<ProductListResponse> productResponses = results.getMappedResults();

        // Execute count aggregation
        long total = executeCountAggregation(criteria);

        // Map shop names
        productResponses.forEach(this::mapShopName);

        // Return paginated result
        return new PageImpl<>(productResponses, pageable, total);
    }

    /**
     * Maps shop name in product response to human-readable format
     */
    private void mapShopName(ProductListResponse response) {
        if (response.getLowestPriceShop() != null) {
            String humanReadableName = productMapper.mapShopNameToHumanReadable(response.getLowestPriceShop());
            response.setLowestPriceShop(humanReadableName);
        }
    }

    private ProductSearchCriteria createSearchCriteria(ProductFiltersDto filters, Pageable pageable) {
        return ProductSearchCriteria.builder()
                .searchName(filters.name())
                .searchCategory(filters.category())
                .shop(filters.shop())
                .minPrice(filters.minPrice())
                .maxPrice(filters.maxPrice())
                .isAvailable(filters.isAvailable())
                .pageable(pageable)
                .build();
    }

    private AggregationResults<ProductListResponse> executeMainAggregation(ProductSearchCriteria criteria) {
        var aggregation = criteria.toAggregation();
        return mongoTemplate.aggregate(aggregation, PRODUCTS_COLLECTION, ProductListResponse.class);
    }

    private long executeCountAggregation(ProductSearchCriteria criteria) {
        var countAggregation = createCountAggregation(criteria);
        AggregationResults<CountResult> countResults =
                mongoTemplate.aggregate(countAggregation, PRODUCTS_COLLECTION, CountResult.class);

        return countResults.getMappedResults().isEmpty()
                ? 0
                : countResults.getMappedResults().getFirst().getCount();
    }

    /**
     * Creates an aggregation for counting results
     */
    private Aggregation createCountAggregation(ProductSearchCriteria criteria) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Add counting operations
        operations.addAll(getCountBaseOperations(criteria));
        operations.addAll(getCountFilterOperations(criteria));
        operations.add(getCountFinalOperation());

        return Aggregation.newAggregation(operations);
    }

    private List<AggregationOperation> getCountBaseOperations(ProductSearchCriteria criteria) {
        List<AggregationOperation> operations = new ArrayList<>();

        // 1. Basic filtering (category, name, etc.)
        operations.add(Aggregation.match(criteria.createBaseCriteria()));

        // 2. Sort the priceHistory by timestamp descending inside each offer to ensure most recent prices are first
        operations.add(context -> new Document(
                ADD_FIELDS,
                new Document(
                        OFFERS,
                        new Document(
                                MAP,
                                new Document(INPUT, "$" + OFFERS)
                                        .append("as", "offer")
                                        .append(
                                                "in",
                                                new Document(
                                                        MERGE_OBJECTS,
                                                        Arrays.asList(
                                                                "$$offer",
                                                                new Document(
                                                                        "priceHistory",
                                                                        new Document(
                                                                                SORT_ARRAY,
                                                                                new Document(
                                                                                        INPUT,
                                                                                        "$$offer.priceHistory")
                                                                                        .append(
                                                                                                "sortBy",
                                                                                                new Document(
                                                                                                        TIMESTAMP,
                                                                                                        -1)))))))))));

        // 3. Unwinding offers
        operations.add(Aggregation.unwind(OFFERS, true));

        // 4. Create temporary fields for shop and its name
        operations.add(Aggregation.addFields()
                .addField("shopObject")
                .withValue(OFFERS_SHOP)
                .addField("shopHumanName")
                .withValue(OFFERS_SHOP)
                .build());

        // 5. Unwinding price history - now we'll get the most recent price for each shop
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        return operations;
    }

    private List<AggregationOperation> getCountFilterOperations(ProductSearchCriteria criteria) {
        List<AggregationOperation> operations = new ArrayList<>();

        // 6. Group by product and shop to find the latest prices for each offer
        operations.add(
                Aggregation.group(Fields.fields().and("productId", "$_id").and("shop", OFFERS_SHOP))
                        .first("offers.priceHistory.price")
                        .as(PRICE)
                        .first(OFFERS_PRICE_HISTORY_TIMESTAMP)
                        .as(TIMESTAMP));

        // 7. Calculate availability status based on timestamp
        operations.add(Aggregation.addFields()
                .addField(IS_AVAILABLE)
                .withValue(ConditionalOperators.when(ComparisonOperators.Gte.valueOf("$" + TIMESTAMP)
                                .greaterThanEqualTo(
                                        DateOperators.DateSubtract.subtractValue(AVAILABILITY_DAYS_THRESHOLD, "day")
                                                .fromDate(NOW)))
                        .then(true)
                        .otherwise(false))
                .build());

        // 8. Filter by availability if requested
        if (criteria.getIsAvailable() != null) {
            operations.add(Aggregation.match(Criteria.where(IS_AVAILABLE).is(criteria.getIsAvailable())));
        }

        // 9. Filter by price
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            operations.add(createPriceRangeFilterOperation(criteria));
        }

        // 10. Group by product
        operations.add(Aggregation.group("_id.productId").count().as("count"));

        return operations;
    }

    private AggregationOperation getCountFinalOperation() {
        return Aggregation.group().count().as("count");
    }

    private AggregationOperation createPriceRangeFilterOperation(ProductSearchCriteria criteria) {
        var priceCriteria = Criteria.where(PRICE);

        if (criteria.getMinPrice() != null) {
            priceCriteria = priceCriteria.gte(criteria.getMinPrice());
        }

        if (criteria.getMaxPrice() != null) {
            priceCriteria = priceCriteria.lte(criteria.getMaxPrice());
        }

        return Aggregation.match(priceCriteria);
    }

    @Getter
    @Setter
    private static class CountResult {
        private long count;
    }

    /**
     * Finds a product by ID and returns it as a ProductDetailResponse
     */
    public ProductDetailResponse findById(String id, Integer priceStampRangeDays) {
        try {
            ObjectId objectId = new ObjectId(id);
            return fetchProductWithAggregation(objectId, priceStampRangeDays);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    /**
     * Fetches a product with aggregation and returns it as ProductDetailResponse
     */
    private ProductDetailResponse fetchProductWithAggregation(ObjectId id, Integer priceStampRangeDays) {
        try {
            // Filter using database aggregation
            var aggregation = createProductDetailAggregation(id, priceStampRangeDays);

            AggregationResults<ProductDetailResponse> results =
                    mongoTemplate.aggregate(aggregation, PRODUCTS_COLLECTION, ProductDetailResponse.class);

            if (results.getMappedResults().isEmpty()) {
                log.debug("No results found for product with ID: {}", id);

                // Check if the product exists with the given ID
                boolean productExists =
                        mongoTemplate.exists(Query.query(Criteria.where("_id").is(id)), Product.class);

                if (!productExists) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
                }

                // If the product exists but has no data in the specified range,
                // return the product structure with empty price history
                var product =
                        mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id)), Product.class);

                return productMapper.toDetailResponse(product);
            }

            return results.getMappedResults().getFirst();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing product data", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process product data from database: " + e.getMessage());
        }
    }

    /**
     * Creates an aggregation to fetch product details
     */
    private Aggregation createProductDetailAggregation(ObjectId id, Integer priceStampRangeDays) {
        List<AggregationOperation> operations = new ArrayList<>();

        // 1. Match product by ID
        operations.add(Aggregation.match(Criteria.where("_id").is(id)));

        // 2. Process date range and create filtering fields
        operations.add(context -> createDateFilteringFieldsOperation(priceStampRangeDays));

        // 3. Add threshold date for availability calculation
        operations.add(Aggregation.addFields()
                .addField("availabilityThresholdDate")
                .withValueOf((AggregationExpression) context -> new Document(
                        "$dateSubtract",
                        new Document("startDate", NOW)
                                .append("unit", DAY)
                                .append("amount", AVAILABILITY_DAYS_THRESHOLD)))
                .build());

        // 4. Filter price history based on date range and calculate availability
        operations.add(context -> {
            var offerMapDoc = createPriceHistoryFilterMapDocWithAvailability();
            return new Document(ADD_FIELDS, new Document(OFFERS, offerMapDoc)); // Using constant
        });

        // 5. Final projection - map fields to match ProductDetailResponse structure
        operations.add(Aggregation.project()
                .and("_id")
                .as("id")
                .and("ean")
                .as("ean")
                .and("name")
                .as("name")
                .and("category")
                .as("category")
                .and("images")
                .as("images")
                .and(OFFERS)
                .as(OFFERS));

        // 6. Remove temporary fields
        operations.add(Aggregation.project()
                .andExclude(
                        "dateRangeStart",
                        "rangeDays",
                        "isZeroDayFilter",
                        "startOfToday",
                        "endOfToday",
                        "availabilityThresholdDate"));

        return Aggregation.newAggregation(operations);
    }

    /**
     * Creates date filtering fields for aggregation
     */
    private Document createDateFilteringFieldsOperation(Integer priceStampRangeDays) {
        var addFieldsDoc = new Document();

        // Get days range value, ensure it's within bounds (1-180 days)
        var rangeDays = priceStampRangeDays != null ?
                Math.clamp(priceStampRangeDays, MIN_PRICE_STAMP_RANGE_DAYS, MAX_PRICE_STAMP_RANGE_DAYS) :
                MIN_PRICE_STAMP_RANGE_DAYS;

        // Add fields with range days value for reference
        addFieldsDoc.append("rangeDays", rangeDays);

        // Add isZeroDayFilter field (always false now as we use minimum of 1 day)
        addFieldsDoc.append("isZeroDayFilter", false);

        // Create date parts for today
        var todayDatePartsDoc = createTodayDatePartsDoc();

        // Add start of today for filtering
        addFieldsDoc.append("startOfToday", new Document(DATE_FROM_PARTS, todayDatePartsDoc));

        // Add end of today (start of tomorrow) for filtering
        var tomorrowDateDoc = createDateAddDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc));
        addFieldsDoc.append("endOfToday", tomorrowDateDoc);

        // Calculate date range start for filtering
        // Adjust the calculation to properly include today's data even for rangeDays = 1
        // We need to subtract (rangeDays) days to get the correct range
        var dateRangeStartDoc = createDateSubtractDocument(
                new Document(DATE_FROM_PARTS, todayDatePartsDoc),
                rangeDays);
        addFieldsDoc.append("dateRangeStart", dateRangeStartDoc);

        return new Document(ADD_FIELDS, addFieldsDoc);
    }

    /**
     * Creates date parts document for today
     */
    private Document createTodayDatePartsDoc() {
        return new Document()
                .append("year", new Document("$year", NOW))
                .append("month", new Document("$month", NOW))
                .append(DAY, new Document("$dayOfMonth", NOW))
                .append("hour", 0)
                .append("minute", 0)
                .append("second", 0);
    }

    /**
     * Creates date add document
     */
    private Document createDateAddDocument(Document startDate) {
        return DateOperators.DateAdd.addValue(1, ProductService.DAY)
                .toDate(startDate)
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Creates date subtract document
     */
    private Document createDateSubtractDocument(Document startDate, int amount) {
        return DateOperators.DateSubtract.subtractValue(amount, ProductService.DAY)
                .fromDate(startDate)
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Creates a map document that filters price history and calculates isAvailable
     */
    private Document createPriceHistoryFilterMapDocWithAvailability() {
        // Create condition for date filtering
        Document dateConditionDoc = createDateFilterConditionDoc();

        // Create filter document for price history filtering
        Document filterOperation = new Document(
                FILTER,
                new Document(INPUT, "$$offer.priceHistory")
                        .append("as", PRICE)
                        .append("cond", dateConditionDoc)); // Using constant

        // Create sortArray operation
        Document sortArrayDoc = new Document(
                SORT_ARRAY,
                new Document(INPUT, filterOperation).append("sortBy", new Document(TIMESTAMP, -1))); // Using constants

        // Create arrayElemAt operation
        Document arrayElemAtDoc = new Document("$arrayElemAt", Arrays.asList(sortArrayDoc, 0));

        // Create let operation for availability check
        Document letDoc = new Document(
                "$let",
                new Document()
                        .append("vars", new Document("latestPrice", arrayElemAtDoc))
                        .append(
                                "in",
                                new Document(
                                        "$gte",
                                        Arrays.asList("$$latestPrice.timestamp", "$availabilityThresholdDate"))));

        // Create isOfferAvailable condition
        Document isOfferAvailableDoc = new Document(
                COND,
                new Document()
                        .append("if", new Document("$eq", Arrays.asList(new Document("$size", filterOperation), 0)))
                        .append("then", false)
                        .append("else", letDoc));

        // Create mergeObjects for the map operation
        Document mergeObjectsDoc = new Document(
                MERGE_OBJECTS,
                new Document("shop", "$$offer.shop")
                        .append("url", "$$offer.url")
                        .append("priceHistory", filterOperation)
                        .append(IS_AVAILABLE, isOfferAvailableDoc));

        // Create the final map operation
        return new Document(
                MAP, 
                new Document()
                        .append(INPUT, "$offers")
                        .append("as", "offer")
                        .append("in", mergeObjectsDoc)); // Using constant
    }

    /**
     * Creates date filter condition document
     */
    private Document createDateFilterConditionDoc() {
        // Create conditional expression to choose appropriate filter
        return new Document(
                COND, // Using constant
                new Document()
                        .append("if", "$isZeroDayFilter")
                        .append("then", createTodayOnlyFilterCondition())
                        .append("else", createStandardDateRangeCondition()));
    }

    /**
     * Creates filter condition for today only
     */
    private Document createTodayOnlyFilterCondition() {
        return BooleanOperators.And.and(
                        ComparisonOperators.Gte.valueOf(PRICE_TIMESTAMP).greaterThanEqualToValue("$startOfToday"),
                        ComparisonOperators.Lt.valueOf(PRICE_TIMESTAMP).lessThanValue("$endOfToday"))
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Creates standard date range condition
     */
    private Document createStandardDateRangeCondition() {
        // Standard filtering: timestamp >= dateRangeStart
        return ComparisonOperators.Gte.valueOf(PRICE_TIMESTAMP)
                .greaterThanEqualToValue("$dateRangeStart")
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Finds a product by ID or throws an exception
     */
    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
