package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.PriceStampResponse;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    // Constants to avoid duplication
    private static final String OFFERS_SHOP = "$offers.shop";
    private static final String GROUP = "$group";
    private static final String MATCH = "$match";
    private static final String NUMERIC_PRICE = "numericPrice";
    private static final String ADD_FIELDS = "$addFields";
    private static final String FIRST = "$first";
    private static final String ID = "_id";
    private static final String PRICE = "price";
    private static final String IS_AVAILABLE = "isAvailable";
    private static final String PRODUCTS_COLLECTION = "products";

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

        // 8. Convert price to number
        operations.add(context -> new Document(
                ADD_FIELDS,
                new Document(
                        NUMERIC_PRICE,
                        new Document(
                                "$convert",
                                new Document("input", "$" + PRICE)
                                        .append("to", "double")
                                        .append("onError", 0.0)
                                        .append("onNull", 0.0)))));

        // 9. Filter by price
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            operations.add(createPriceRangeFilterOperation(criteria));
        }

        // 10. Group by product
        operations.add(context ->
                new Document(GROUP, new Document(ID, "$_id.productId").append("count", new Document("$sum", 1))));

        return operations;
    }

    private AggregationOperation getCountFinalOperation() {
        // 11. Count unique products
        return context -> new Document(GROUP, new Document(ID, null).append("count", new Document("$sum", 1)));
    }

    private AggregationOperation createPriceRangeFilterOperation(ProductSearchCriteria criteria) {
        return context -> {
            Document matchDoc = new Document(MATCH, new Document());

            if (criteria.getMinPrice() != null) {
                matchDoc.get(MATCH, Document.class)
                        .append(
                                NUMERIC_PRICE,
                                new Document("$gte", criteria.getMinPrice().doubleValue()));
            }

            if (criteria.getMaxPrice() != null) {
                Document numericPriceDoc = matchDoc.get(MATCH, Document.class).get(NUMERIC_PRICE, Document.class);
                if (numericPriceDoc == null) {
                    matchDoc.get(MATCH, Document.class)
                            .append(
                                    NUMERIC_PRICE,
                                    new Document("$lte", criteria.getMaxPrice().doubleValue()));
                } else {
                    numericPriceDoc.append("$lte", criteria.getMaxPrice().doubleValue());
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
        // Find product
        Product product = findProductOrThrow(id);

        // Map to response
        ProductDetailResponse response = productMapper.toDetailResponse(product);

        // Filter price history based on range
        if (priceStampRangeDays != null) {
            filterPriceHistoryByRange(response, validatePriceStampRange(priceStampRangeDays));
        }

        return response;
    }

    /**
     * Filters price history in product offers based on date range
     */
    private void filterPriceHistoryByRange(ProductDetailResponse response, int priceStampRangeDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(priceStampRangeDays);

        if (response.getOffers() != null) {
            response.getOffers().forEach(offer -> {
                if (offer.getPriceHistory() != null) {
                    List<PriceStampResponse> filteredHistory = offer.getPriceHistory().stream()
                            .filter(price -> price.getTimestamp().isAfter(startDate))
                            .toList();
                    offer.setPriceHistory(filteredHistory);
                }
            });
        }
    }

    private int validatePriceStampRange(Integer priceStampRangeDays) {
        return priceStampRangeDays < 0 || priceStampRangeDays > 180 ? 90 : priceStampRangeDays;
    }

    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
