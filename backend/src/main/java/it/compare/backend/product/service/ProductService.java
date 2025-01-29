package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponse> findAll(ProductFiltersDto filters, Pageable pageable) {
        var criteria = ProductSearchCriteria.builder()
                .name(filters.name())
                .category(filters.category())
                .shop(filters.shop())
                .minPrice(filters.minPrice())
                .maxPrice(filters.maxPrice())
                .pageable(pageable)
                .build();

        if (criteria.requiresInMemoryProcessing()) {
            var products = mongoTemplate.find(criteria.toQuery(), Product.class);
            var responses = products.stream().map(productMapper::toListResponse).toList();
            responses = criteria.applyPriceFiltering(responses);
            responses = criteria.applySorting(responses);
            return criteria.applyPagination(responses);
        } else {
            var query = criteria.toQuery();
            var totalQuery = Query.of(query).limit(0).skip(0);
            var total = mongoTemplate.count(totalQuery, Product.class);

            query.with(pageable);
            var products = mongoTemplate.find(query, Product.class);

            var responses = products.stream().map(productMapper::toListResponse).toList();
            return new PageImpl<>(responses, pageable, total);
        }
    }

    public ProductDetailResponse findById(String id, Integer priceStampRangeDays) {
        int days = priceStampRangeDays < 0 || priceStampRangeDays > 180 ? 90 : priceStampRangeDays;
        return productMapper.toDetailResponse(findProductOrThrow(id), days);
    }

    public Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
