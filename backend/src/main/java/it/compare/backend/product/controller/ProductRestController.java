package it.compare.backend.product.controller;

import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductListResponse> getProducts(Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse getProduct(@PathVariable String id) {
        return productService.findById(id);
    }
}
