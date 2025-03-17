package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
    private static final String GROUP = "$group";
    private static final String MATCH = "$match";
    private static final String ADD_FIELDS = "$addFields";
    private static final String FIRST = "$first";
    private static final String ID = "_id";
    private static final String PRICE = "price";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String PRODUCTS_COLLECTION = "products";
    private static final String DATE_FROM_PARTS = "$dateFromParts";
    private static final String PRICE_TIMESTAMP = "$$price.timestamp";
    private static final String NOW = "$$NOW";
    private static final String UNIT = "unit";
    private static final String START_DATE = "startDate";
    private static final String AMOUNT = "amount";
    private static final String DAY = "day";

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
                .pageable(pageable)
                .build();
    }

    private AggregationResults<ProductListResponse> executeMainAggregation(ProductSearchCriteria criteria) {
        Aggregation aggregation = criteria.toAggregation();
        return mongoTemplate.aggregate(aggregation, PRODUCTS_COLLECTION, ProductListResponse.class);
    }

    private long executeCountAggregation(ProductSearchCriteria criteria) {
        Aggregation countAggregation = createCountAggregation(criteria);
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

        // 2. Unwinding offers
        operations.add(Aggregation.unwind("offers", true));

        // 3. Create temporary fields for shop and its name
        operations.add(context ->
                new Document(ADD_FIELDS, new Document("shopObject", OFFERS_SHOP).append("shopHumanName", OFFERS_SHOP)));

        // 4. Sort offers by time to find the latest prices
        operations.add(context -> new Document(
                "$sort", new Document(ID, 1).append("offers.shop", 1).append("offers.priceHistory.timestamp", -1)));

        // 5. Unwinding price history
        operations.add(Aggregation.unwind("offers.priceHistory", true));

        return operations;
    }

    private List<AggregationOperation> getCountFilterOperations(ProductSearchCriteria criteria) {
        List<AggregationOperation> operations = new ArrayList<>();

        // 6. Group by product and shop to find the latest prices for each offer
        operations.add(context -> new Document(
                GROUP,
                new Document(ID, new Document("productId", "$_id").append("shop", OFFERS_SHOP))
                        .append(PRICE, new Document(FIRST, "$offers.priceHistory.price"))
                        .append(IS_AVAILABLE, new Document(FIRST, "$offers.priceHistory.isAvailable"))));

        // 7. Filter unavailable offers
        operations.add(Aggregation.match(Criteria.where(IS_AVAILABLE).is(true)));

        // 8. Filter by price
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            operations.add(createPriceRangeFilterOperation(criteria));
        }

        // 9. Group by product
        operations.add(context ->
                new Document(GROUP, new Document(ID, "$_id.productId").append("count", new Document("$sum", 1))));

        return operations;
    }

    private AggregationOperation getCountFinalOperation() {
        // 10. Count unique products
        return context -> new Document(GROUP, new Document(ID, null).append("count", new Document("$sum", 1)));
    }

    private AggregationOperation createPriceRangeFilterOperation(ProductSearchCriteria criteria) {
        return context -> {
            Document matchDoc = new Document(MATCH, new Document());

            if (criteria.getMinPrice() != null) {
                matchDoc.get(MATCH, Document.class).append(PRICE, new Document("$gte", criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                Document priceDoc = matchDoc.get(MATCH, Document.class).get(PRICE, Document.class);
                if (priceDoc == null) {
                    matchDoc.get(MATCH, Document.class).append(PRICE, new Document("$lte", criteria.getMaxPrice()));
                } else {
                    priceDoc.append("$lte", criteria.getMaxPrice());
                }
            }

            return matchDoc;
        };
    }

    @Getter
    @Setter
    private static class CountResult {
        private long count;
    }

    public ProductDetailResponse findById(String id, Integer priceStampRangeDays) {
        try {
            // Use product id and filter data on database side
            Product product = fetchProductWithAggregation(id, priceStampRangeDays);
            return productMapper.toDetailResponse(product);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid product ID format");
        }
    }

    private Product fetchProductWithAggregation(String id, Integer priceStampRangeDays) {
        try {
            // Filter using database aggregation
            Aggregation aggregation = createProductDetailAggregation(id, priceStampRangeDays);
            AggregationResults<Product> results =
                    mongoTemplate.aggregate(aggregation, PRODUCTS_COLLECTION, Product.class);

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
                log.debug("Product exists but no data in specified range, returning basic product structure");
                return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id)), Product.class);
            }

            return results.getMappedResults().getFirst();
        } catch (ResponseStatusException e) {
            throw e; // Re-throw response status exceptions
        } catch (Exception e) {
            log.error("Error processing product data", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process product data from database: " + e.getMessage());
        }
    }

    private Aggregation createProductDetailAggregation(String id, Integer priceStampRangeDays) {
        List<AggregationOperation> operations = new ArrayList<>();

        // 1. Match product by ID - używamy bezpośrednio stringa bez konwersji na ObjectId
        operations.add(context -> new Document(MATCH, new Document("_id", id)));

        // 2. Process date range and create filtering fields
        operations.add(context -> createDateFilteringFieldsOperation(priceStampRangeDays));

        // 3. Filter price history based on date range
        operations.add(context -> {
            Document offerMapDoc = createPriceHistoryFilterMapDoc();
            return new Document(ADD_FIELDS, new Document("offers", offerMapDoc));
        });

        // 4. Remove temporary fields
        operations.add(Aggregation.project()
                .andExclude("dateRangeStart", "rangeDays", "isZeroDayFilter", "startOfToday", "endOfToday"));

        return Aggregation.newAggregation(operations);
    }

    private Document createDateFilteringFieldsOperation(Integer priceStampRangeDays) {
        Document addFieldsDoc = new Document();

        // Get days range value and determine if it's today-only filter
        int rangeDays = priceStampRangeDays != null ? Math.max(0, priceStampRangeDays) : 0;
        addFieldsDoc.append("rangeDays", rangeDays);
        addFieldsDoc.append("isZeroDayFilter", rangeDays == 0);

        // Create date parts for today
        Document todayDatePartsDoc = createTodayDatePartsDoc();

        // Add start of today for filtering
        addFieldsDoc.append("startOfToday", new Document(DATE_FROM_PARTS, todayDatePartsDoc));

        // Add end of today (start of tomorrow) for filtering
        Document tomorrowDateDoc = createDateAddDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc), DAY, 1);
        addFieldsDoc.append("endOfToday", tomorrowDateDoc);

        // Add date range start for standard filtering (non-zero days)
        if (rangeDays > 0) {
            Document dateRangeStartDoc =
                    createDateSubtractDocument(new Document(DATE_FROM_PARTS, todayDatePartsDoc), DAY, rangeDays);
            addFieldsDoc.append("dateRangeStart", dateRangeStartDoc);
        }

        return new Document(ADD_FIELDS, addFieldsDoc);
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

    private Document createDateAddDocument(Document startDate, String unit, int amount) {
        return new Document(
                "$dateAdd",
                new Document(START_DATE, startDate).append(UNIT, unit).append(AMOUNT, amount));
    }

    private Document createDateSubtractDocument(Document startDate, String unit, int amount) {
        return new Document(
                "$dateSubtract",
                new Document(START_DATE, startDate).append(UNIT, unit).append(AMOUNT, amount));
    }

    private Document createPriceHistoryFilterMapDoc() {
        // Create condition for date filtering
        Document dateConditionDoc = createDateFilterConditionDoc();

        // Create filter document
        Document filterDoc = new Document()
                .append("input", "$$offer.priceHistory")
                .append("as", PRICE)
                .append("cond", dateConditionDoc);

        // Create map for price history filtering
        Document inDoc = new Document()
                .append(
                        "$mergeObjects",
                        List.of("$$offer", new Document("priceHistory", new Document("$filter", filterDoc))));

        // Create final map document
        return new Document(
                "$map",
                new Document().append("input", "$offers").append("as", "offer").append("in", inDoc));
    }

    private Document createDateFilterConditionDoc() {
        // Create conditional expression to choose appropriate filter
        return new Document(
                "$cond",
                new Document()
                        .append("if", "$isZeroDayFilter")
                        .append("then", createTodayOnlyFilterCondition())
                        .append("else", createStandardDateRangeCondition()));
    }

    private Document createTodayOnlyFilterCondition() {
        // First condition: timestamp >= startOfToday
        Document gteStartOfToday = new Document("$gte", List.of(PRICE_TIMESTAMP, "$startOfToday"));

        // Second condition: timestamp < endOfToday (start of tomorrow)
        Document ltEndOfToday = new Document("$lt", List.of(PRICE_TIMESTAMP, "$endOfToday"));

        // Combine conditions with $and
        return new Document("$and", List.of(gteStartOfToday, ltEndOfToday));
    }

    private Document createStandardDateRangeCondition() {
        // Standard filtering: timestamp >= dateRangeStart
        return new Document("$gte", List.of(PRICE_TIMESTAMP, "$dateRangeStart"));
    }

    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
