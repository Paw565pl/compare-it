package it.compare.backend.product.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.product.dto.ProductFiltersDto;
import it.compare.backend.product.response.ProductDetailResponse;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import it.compare.backend.product.validator.ValidProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductListResponse> findAll(
            ProductFiltersDto filters,
            @PageableDefault(size = 20, sort = "offersCount", direction = DESC) Pageable pageable) {
        return productService.findAll(filters, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponse findById(
            @ValidProductId @PathVariable String id,
            @RequestParam(required = false, defaultValue = "90") Integer priceStampRangeDays) {
        return productService.findById(id, priceStampRangeDays);
    }
}
