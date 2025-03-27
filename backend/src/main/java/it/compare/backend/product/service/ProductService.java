package it.compare.backend.product.service;

import it.compare.backend.product.aggregation.ProductAggregationBuilder;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String NOW = "$$NOW";
    private static final String DAY = "day";
    private static final int MAX_PRICE_STAMP_RANGE_DAYS = 180;
    private static final int MIN_PRICE_STAMP_RANGE_DAYS = 1;
    public static final int AVAILABILITY_DAYS_THRESHOLD = 3;

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    private record CountResult(long count) {}

    public Page<ProductListResponse> findAll(ProductFiltersDto filters, Pageable pageable) {
        var aggregationBuilder = createAggregationBuilder(filters, pageable);

        var results = mongoTemplate.aggregate(aggregationBuilder.buildSearchAggregation(), ProductListResponse.class);
        var productResponses = results.getMappedResults();

        var total = executeCountAggregation(aggregationBuilder);

        return new PageImpl<>(productResponses, pageable, total);
    }

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

    private long executeCountAggregation(ProductAggregationBuilder builder) {
        var countAggregation = builder.buildCountAggregation();
        var countResults = mongoTemplate.aggregate(countAggregation, CountResult.class);

        var countResult = countResults.getUniqueMappedResult();
        return countResult != null ? countResult.count() : 0;
    }

    public ProductDetailResponse findById(String id, Integer priceStampRangeDays) {
        try {
            var objectId = new ObjectId(id);
            return fetchProductWithAggregation(objectId, priceStampRangeDays);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    private ProductDetailResponse fetchProductWithAggregation(ObjectId id, Integer priceStampRangeDays) {
        var aggregation = createProductDetailAggregation(id, priceStampRangeDays);
        var results = mongoTemplate.aggregate(aggregation, ProductDetailResponse.class);

        var detailResponse = results.getUniqueMappedResult();

        if (detailResponse == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        return detailResponse;
    }

    /**
     * Creates an aggregation to fetch product details
     */
    private TypedAggregation<Product> createProductDetailAggregation(ObjectId id, Integer priceStampRangeDays) {
        var operations = new ArrayList<AggregationOperation>();

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

        return Aggregation.newAggregation(Product.class, operations);
    }

    private Document createDateFilteringFieldsOperation(Integer priceStampRangeDays) {
        var addFieldsDoc = new Document();

        var rangeDays = priceStampRangeDays != null
                ? Math.clamp(priceStampRangeDays, MIN_PRICE_STAMP_RANGE_DAYS, MAX_PRICE_STAMP_RANGE_DAYS)
                : MIN_PRICE_STAMP_RANGE_DAYS;

        addFieldsDoc.append("rangeDays", rangeDays);

        addFieldsDoc.append("isZeroDayFilter", false);

        var todayDatePartsDoc = createTodayDatePartsDoc();

        addFieldsDoc.append("startOfToday", new Document(DATE_FROM_PARTS, todayDatePartsDoc));

        var tomorrowDateDoc = createDateAddDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc));
        addFieldsDoc.append("endOfToday", tomorrowDateDoc);

        var dateRangeStartDoc = createDateSubtractDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc), rangeDays);
        addFieldsDoc.append("dateRangeStart", dateRangeStartDoc);

        return new Document("$addFields", addFieldsDoc);
    }

    private Document createTodayDatePartsDoc() {
        return new Document()
                .append("year", new Document("$year", NOW))
                .append("month", new Document("$month", NOW))
                .append(DAY, new Document("$dayOfMonth", NOW))
                .append("hour", 0)
                .append("minute", 0)
                .append("second", 0);
    }

    private Document createDateAddDocument(Document startDate) {
        return DateOperators.DateAdd.addValue(1, DAY).toDate(startDate).toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    private Document createDateSubtractDocument(Document startDate, int amount) {
        return DateOperators.DateSubtract.subtractValue(amount, DAY)
                .fromDate(startDate)
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    private Document createPriceHistoryFilterMapDocWithAvailability() {
        var dateConditionDoc = createDateFilterConditionDoc();

        var filterOperation = new Document(
                "$filter",
                new Document(INPUT, "$$offer.priceHistory")
                        .append("as", "price")
                        .append("cond", dateConditionDoc));

        var sortArrayDoc = new Document(
                "$sortArray", new Document(INPUT, filterOperation).append("sortBy", new Document("timestamp", -1)));

        var arrayElemAtDoc = new Document("$arrayElemAt", Arrays.asList(sortArrayDoc, 0));

        var letDoc = new Document(
                "$let",
                new Document()
                        .append("vars", new Document("latestPrice", arrayElemAtDoc))
                        .append(
                                "in",
                                new Document(
                                        "$gte",
                                        Arrays.asList("$$latestPrice.timestamp", "$availabilityThresholdDate"))));

        var isOfferAvailableDoc = new Document(
                "$cond",
                new Document()
                        .append("if", new Document("$eq", Arrays.asList(new Document("$size", filterOperation), 0)))
                        .append("then", false)
                        .append("else", letDoc));

        var mergeObjectsDoc = new Document(
                "$mergeObjects",
                new Document("shop", "$$offer.shop")
                        .append("url", "$$offer.url")
                        .append("priceHistory", filterOperation)
                        .append("isAvailable", isOfferAvailableDoc));

        return new Document(
                "$map",
                new Document().append(INPUT, "$offers").append("as", "offer").append("in", mergeObjectsDoc));
    }

    private Document createDateFilterConditionDoc() {
        return new Document(
                "$cond",
                new Document()
                        .append("if", "$isZeroDayFilter")
                        .append("then", createTodayOnlyFilterCondition())
                        .append("else", createStandardDateRangeCondition()));
    }

    private Document createTodayOnlyFilterCondition() {
        return BooleanOperators.And.and(
                        ComparisonOperators.Gte.valueOf(PRICE_TIMESTAMP).greaterThanEqualToValue("$startOfToday"),
                        ComparisonOperators.Lt.valueOf(PRICE_TIMESTAMP).lessThanValue("$endOfToday"))
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    private Document createStandardDateRangeCondition() {
        return ComparisonOperators.Gte.valueOf(PRICE_TIMESTAMP)
                .greaterThanEqualToValue("$dateRangeStart")
                .toDocument(Aggregation.DEFAULT_CONTEXT);
    }

    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
