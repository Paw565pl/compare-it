package it.compare.backend.product.controller;

import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import java.util.Arrays;
import java.util.List;
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
    public Page<ProductListResponse> findAll(ProductFiltersDto filters, Pageable pageable) {
        return productService.findAll(filters, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @GetMapping("/shops")
    public List<String> getAllShops() {
        return Arrays.stream(Shop.values()).map(Shop::getHumanReadableName).toList();
    }

    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return Arrays.stream(Category.values())
                .map(Category::getHumanReadableName)
                .toList();
    }
}
