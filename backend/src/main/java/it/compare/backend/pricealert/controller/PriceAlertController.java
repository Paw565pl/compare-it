package it.compare.backend.pricealert.controller;

import it.compare.backend.pricealert.dto.PriceAlertDto;
import it.compare.backend.pricealert.response.PriceAlertResponse;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final ProductService productService;
    @IsAuthenticated
    @GetMapping("/price-alerts")
    public Page<PriceAlertResponse> getCurrentUserAlerts(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        return priceAlertService.findAllByUser(OAuthUserDetails.fromJwt(jwt), pageable);
    }

    @IsAuthenticated
    @PostMapping("/products/{productId}/price-alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceAlertResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @Valid @RequestBody PriceAlertDto alertDto) {
        return priceAlertService.create(OAuthUserDetails.fromJwt(jwt), productId, alertDto);
    }

    @IsAuthenticated
    @DeleteMapping("/price-alerts/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable String alertId) {
        priceAlertService.deleteAlert(OAuthUserDetails.fromJwt(jwt), alertId);
    }

    @GetMapping("/products/{productId}/check-price-alerts")
    public void checkPriceAlertsForProduct(@PathVariable String productId) {
        var product = productService.findProductOrThrow(productId);
        priceAlertService.checkPriceAlerts(product);
    }
}
