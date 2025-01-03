package it.compare.backend.product.controller;

import it.compare.backend.product.filter.ProductFilter;
import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
            Pageable pageable) {

        ProductFilter filter = new ProductFilter();
        filter.setName(name);

        if (category != null) {
            Category validCategory = Arrays.stream(Category.values())
                    .filter(c -> c.getHumanReadableName().equals(category))
                    .findFirst()
                    .orElseThrow(
                            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category: " + category));
            filter.setCategory(validCategory);
        }

        if (shop != null) {
            Shop validShop = Arrays.stream(Shop.values())
                    .filter(s -> s.getHumanReadableName().equals(shop))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid shop: " + shop));
            filter.setShop(validShop);
        }

        return productService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse findById(@PathVariable String id) {
        return productService.findById(id);
    }
}
