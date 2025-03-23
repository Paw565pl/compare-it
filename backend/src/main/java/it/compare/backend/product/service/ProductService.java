package it.compare.backend.product.service;

import it.compare.backend.product.aggregation.ProductAggregationBuilder;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    // Constants
    private static final String DATE_FROM_PARTS = "$dateFromParts";
    private static final String OFFERS = "offers";
    private static final String INPUT = "input";
    private static final String PRICE_TIMESTAMP = "$$price.timestamp";
    private static final String PRODUCTS = "products";
    private static final String NOW = "$$NOW";
    private static final String DAY = "day";
    private static final int MAX_PRICE_STAMP_RANGE_DAYS = 180;
    private static final int MIN_PRICE_STAMP_RANGE_DAYS = 1;
    private static final int AVAILABILITY_DAYS_THRESHOLD = 3;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    private record CountResult(long count) {}
    /**
     * Finds all products matching given filters with pagination
     */
    public Page<ProductListResponse> findAll(ProductFiltersDto filters, Pageable pageable) {
        // Create aggregation builder from filters
        var aggregationBuilder = createAggregationBuilder(filters, pageable);

        // Execute main aggregation
        AggregationResults<ProductListResponse> results = mongoTemplate.aggregate(
                aggregationBuilder.buildSearchAggregation(), PRODUCTS, ProductListResponse.class);
        List<ProductListResponse> productResponses = results.getMappedResults();

        // Execute count aggregation
        var total = executeCountAggregation(aggregationBuilder);

        // Return paginated result
        return new PageImpl<>(productResponses, pageable, total);
    }

    /**
     * Creates an aggregation builder from filters
     */
    private ProductAggregationBuilder createAggregationBuilder(ProductFiltersDto filters, Pageable pageable) {
        return ProductAggregationBuilder.builder()
                .searchName(filters.name())
                .searchCategory(filters.category())
                .shop(filters.shop())
                .minPrice(filters.minPrice())
                .maxPrice(filters.maxPrice())
                .isAvailable(filters.isAvailable())
                .pageable(pageable)
                .build();
    }

    /**
     * Executes count aggregation and returns the count
     */
    private long executeCountAggregation(ProductAggregationBuilder builder) {
        var countAggregation = builder.buildCountAggregation();
        AggregationResults<CountResult> countResults =
                mongoTemplate.aggregate(countAggregation, PRODUCTS, CountResult.class);

        var countResult = countResults.getUniqueMappedResult();
        return countResult != null ? countResult.count() : 0;
    }

    /**
     * Finds a product by ID and returns it as a ProductDetailResponse
     */
    public ProductDetailResponse findById(String id, Integer priceStampRangeDays) {
        try {
            var objectId = new ObjectId(id);
            return fetchProductWithAggregation(objectId, priceStampRangeDays);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    /**
     * Fetches a product with aggregation and returns it as ProductDetailResponse
     */
    private ProductDetailResponse fetchProductWithAggregation(ObjectId id, Integer priceStampRangeDays) {
        // Create and execute aggregation
        var aggregation = createProductDetailAggregation(id, priceStampRangeDays);
        AggregationResults<ProductDetailResponse> results =
                mongoTemplate.aggregate(aggregation, PRODUCTS, ProductDetailResponse.class);

        var detailResponse = results.getUniqueMappedResult();

        if (detailResponse == null) {
            log.debug("No results found for product with ID: {}", id);

            // Check if the product exists with the given ID
            var productExists =
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

        return detailResponse;
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
            return new Document("$addFields", new Document(OFFERS, offerMapDoc));
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
        var rangeDays = priceStampRangeDays != null
                ? Math.clamp(priceStampRangeDays, MIN_PRICE_STAMP_RANGE_DAYS, MAX_PRICE_STAMP_RANGE_DAYS)
                : MIN_PRICE_STAMP_RANGE_DAYS;

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
        var dateRangeStartDoc = createDateSubtractDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc), rangeDays);
        addFieldsDoc.append("dateRangeStart", dateRangeStartDoc);

        return new Document("$addFields", addFieldsDoc);
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
        return DateOperators.DateAdd.addValue(1, DAY).toDate(startDate).toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Creates date subtract document
     */
    private Document createDateSubtractDocument(Document startDate, int amount) {
        return DateOperators.DateSubtract.subtractValue(amount, DAY)
                .fromDate(startDate)
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    /**
     * Creates a map document that filters price history and calculates isAvailable
     */
    private Document createPriceHistoryFilterMapDocWithAvailability() {
        // Create condition for date filtering
        var dateConditionDoc = createDateFilterConditionDoc();

        // Create filter document for price history filtering
        var filterOperation = new Document(
                "$filter",
                new Document(INPUT, "$$offer.priceHistory")
                        .append("as", "price")
                        .append("cond", dateConditionDoc));

        // Create sortArray operation
        var sortArrayDoc = new Document(
                "$sortArray", new Document(INPUT, filterOperation).append("sortBy", new Document("timestamp", -1)));

        // Create arrayElemAt operation
        var arrayElemAtDoc = new Document("$arrayElemAt", Arrays.asList(sortArrayDoc, 0));

        // Create let operation for availability check
        var letDoc = new Document(
                "$let",
                new Document()
                        .append("vars", new Document("latestPrice", arrayElemAtDoc))
                        .append(
                                "in",
                                new Document(
                                        "$gte",
                                        Arrays.asList("$$latestPrice.timestamp", "$availabilityThresholdDate"))));

        // Create isOfferAvailable condition
        var isOfferAvailableDoc = new Document(
                "$cond",
                new Document()
                        .append("if", new Document("$eq", Arrays.asList(new Document("$size", filterOperation), 0)))
                        .append("then", false)
                        .append("else", letDoc));

        // Create mergeObjects for the map operation
        var mergeObjectsDoc = new Document(
                "$mergeObjects",
                new Document("shop", "$$offer.shop")
                        .append("url", "$$offer.url")
                        .append("priceHistory", filterOperation)
                        .append("isAvailable", isOfferAvailableDoc));

        // Create the final map operation
        return new Document(
                "$map",
                new Document().append(INPUT, "$offers").append("as", "offer").append("in", mergeObjectsDoc));
    }

    /**
     * Creates date filter condition document
     */
    private Document createDateFilterConditionDoc() {
        // Create conditional expression to choose appropriate filter
        return new Document(
                "$cond",
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
