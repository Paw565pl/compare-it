package it.compare.backend.product.controller;

import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductListResponse> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String shop,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return productService.findAll(name, category, shop, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse findById(@PathVariable String id) {
        return productService.findById(id);
    }
}
