package it.compare.backend.product.controller;

import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
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
    public ProductDetailResponse findById(
            @PathVariable String id, @RequestParam(required = false, defaultValue = "90") Integer priceStampRangeDays) {
        int days = priceStampRangeDays < 0 || priceStampRangeDays > 180 ? 90 : priceStampRangeDays;
        return productService.findById(id, days);
    }
}
