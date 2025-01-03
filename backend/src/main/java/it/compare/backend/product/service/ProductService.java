package it.compare.backend.product.service;

import it.compare.backend.product.filter.ProductFilter;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.query.ProductQueryBuilder;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import java.math.BigDecimal;
import java.util.List;
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

    public Page<ProductListResponse> findAll(ProductFilter filter, Pageable pageable) {
        Query query = ProductQueryBuilder.buildQuery(filter);

        long total = mongoTemplate.count(query, Product.class);
        query.with(pageable);
        List<Product> products = mongoTemplate.find(query, Product.class);

        List<ProductListResponse> sortedResponses = products.stream()
                .map(productMapper::toListResponse)
                .sorted((r1, r2) -> {
                    var order = pageable.getSort().getOrderFor("lowestCurrentPrice");

                    if (order != null) {
                        boolean ascending = order.isAscending();
                        BigDecimal price1 = r1.getLowestCurrentPrice();
                        BigDecimal price2 = r2.getLowestCurrentPrice();
                        return ascending ? price1.compareTo(price2) : price2.compareTo(price1);
                    }
                    return 0;
                })
                .toList();

        return new PageImpl<>(sortedResponses, pageable, total);
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
