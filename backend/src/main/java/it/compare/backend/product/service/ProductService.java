package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponse> findAll(
            String name, String category, String shop, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .name(name)
                .category(category)
                .shop(shop)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .pageable(pageable)
                .build();

        List<Product> products = mongoTemplate.find(criteria.toQuery(), Product.class);

        List<ProductListResponse> responses =
                products.stream().map(productMapper::toListResponse).toList();

        responses = criteria.applyPriceFiltering(responses);
        responses = criteria.applySorting(responses);
        return criteria.applyPagination(responses);
    }

    public ProductDetailResponse findById(String id) {
        return productMapper.toDetailResponse(findProductOrThrow(id));
    }

    private Product findProductOrThrow(String id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
