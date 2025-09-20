package it.compare.backend.product.service;

import it.compare.backend.product.dto.ProductDetailResponseDto;
import it.compare.backend.product.dto.ProductFilterDto;
import it.compare.backend.product.dto.ProductListResponseDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String BEST_OFFER_PRICE_FIELD = "computedState.bestOffer.price";
    private static final String AVAILABLE_OFFERS_COUNT_FIELD = "computedState.availableOffersCount";

    private static final Map<String, String> validSortPropertiesMap = Map.of(
            "name", "name",
            "lowestcurrentprice", BEST_OFFER_PRICE_FIELD,
            "availableofferscount", AVAILABLE_OFFERS_COUNT_FIELD);
    private static final int MAX_PRICE_STAMP_RANGE_DAYS = 180;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponseDto> findAll(ProductFilterDto filters, Pageable pageable) {
        var query = new Query();

        if (filters.name() != null && !filters.name().isBlank())
            query.addCriteria(TextCriteria.forDefaultLanguage()
                    .matching("\"" + filters.name() + "\"")
                    .caseSensitive(false));
        if (filters.category() != null)
            query.addCriteria(Criteria.where("category").is(filters.category().name()));
        if (filters.shops() != null && !filters.shops().isEmpty())
            query.addCriteria(Criteria.where("offers.shop")
                    .in(filters.shops().stream().map(Enum::name).toList()));

        var priceFilterDocument = new Document();
        if (filters.minPrice() != null) priceFilterDocument.append("$gte", new Decimal128(filters.minPrice()));
        if (filters.maxPrice() != null) priceFilterDocument.append("$lte", new Decimal128(filters.maxPrice()));
        if (!priceFilterDocument.isEmpty())
            query.addCriteria(Criteria.where(BEST_OFFER_PRICE_FIELD).is(priceFilterDocument));

        if (filters.isAvailable() != null)
            query.addCriteria(
                    Boolean.TRUE.equals(filters.isAvailable())
                            ? Criteria.where(AVAILABLE_OFFERS_COUNT_FIELD).gte(1)
                            : Criteria.where(AVAILABLE_OFFERS_COUNT_FIELD).is(0));

        var total = mongoTemplate.count(query, Product.class);
        if (total == 0) return Page.empty(pageable);

        var sortOrders = getListSortOrders(pageable);
        query.with(Sort.by(sortOrders));

        query.skip(pageable.getOffset());
        query.limit(pageable.getPageSize());

        var products = mongoTemplate.find(query, Product.class);
        var content = products.stream().map(productMapper::toListResponseDto).toList();

        return new PageImpl<>(content, pageable, total);
    }

    public List<Sort.Order> getListSortOrders(Pageable pageable) {
        var sortOrders = new ArrayList<>(pageable.getSort().stream()
                .filter(o -> validSortPropertiesMap.containsKey(o.getProperty().toLowerCase()))
                .map(o -> new Sort.Order(
                        o.getDirection(),
                        validSortPropertiesMap.get(o.getProperty().toLowerCase())))
                .toList());
        sortOrders.add(Sort.Order.asc("_id"));

        return sortOrders;
    }

    public ProductDetailResponseDto findById(String id, Integer priceStampRangeDays) {
        var clampedPriceStampRangeDays = Math.clamp(priceStampRangeDays, 0, MAX_PRICE_STAMP_RANGE_DAYS);
        var cutOff = Instant.now().minus(Duration.ofDays(clampedPriceStampRangeDays));

        var matchOperation = Aggregation.match(Criteria.where("_id").is(new ObjectId(id)));

        var filterPriceHistoryOperation = ArrayOperators.Filter.filter("$$offer.priceHistory")
                .as("priceHistoryEntry")
                .by(ComparisonOperators.Gte.valueOf("$$priceHistoryEntry.timestamp")
                        .greaterThanEqualToValue(cutOff));
        var mapOffersOperation = VariableOperators.Map.itemsOf("offers")
                .as("offer")
                .andApply(ObjectOperators.MergeObjects.merge("$$offer")
                        .mergeWith(new Document("priceHistory", filterPriceHistoryOperation)));
        var projectOperation = Aggregation.project()
                .andInclude("ean", "name", "category", "images")
                .and(mapOffersOperation)
                .as("offers");

        var aggregation = Aggregation.newAggregation(Product.class, matchOperation, projectOperation);
        var result = mongoTemplate
                .aggregate(aggregation, ProductDetailResponseDto.class)
                .getUniqueMappedResult();

        if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");

        return result;
    }

    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found."));
    }
}
