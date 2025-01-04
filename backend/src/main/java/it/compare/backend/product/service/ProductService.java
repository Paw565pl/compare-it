package it.compare.backend.product.service;

import it.compare.backend.product.criteria.ProductSearchCriteria;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponse> findAll(String name, String category, String shop, Pageable pageable) {
        ProductSearchCriteria productSearchCriteria = ProductSearchCriteria.builder()
                .name(name)
                .category(category)
                .shop(shop)
                .build();
        Criteria criteria = productSearchCriteria.toCriteria();
        Query query = Query.query(criteria).with(pageable);

        long total = mongoTemplate.count(Query.query(criteria), Product.class);
        List<Product> products = mongoTemplate.find(query, Product.class);

        return new PageImpl<>(
                products.stream().map(productMapper::toListResponse).toList(),
                pageable,
                total
        );
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
