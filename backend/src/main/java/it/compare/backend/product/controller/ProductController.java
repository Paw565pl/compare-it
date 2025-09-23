package it.compare.backend.product.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.product.dto.ProductDetailResponseDto;
import it.compare.backend.product.dto.ProductFilterDto;
import it.compare.backend.product.dto.ProductListResponseDto;
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
    public Page<ProductListResponseDto> findAll(
            ProductFilterDto filters,
            @PageableDefault(size = 20, sort = "availableOffersCount", direction = DESC) Pageable pageable) {
        return productService.findAll(filters, pageable);
    }

    @GetMapping("/{id}")
    public ProductDetailResponseDto findById(
            @ValidProductId @PathVariable String id,
            @RequestParam(required = false, defaultValue = "30") Integer priceStampRangeDays) {
        return productService.findById(id, priceStampRangeDays);
    }
}
