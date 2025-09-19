package it.compare.backend.pricealert.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.pricealert.dto.PriceAlertFilterDto;
import it.compare.backend.pricealert.dto.PriceAlertRequestDto;
import it.compare.backend.pricealert.dto.PriceAlertResponseDto;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.pricealert.validator.ValidPriceAlertId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@IsAuthenticated
@RequiredArgsConstructor
@RequestMapping("/api/v1/price-alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @GetMapping
    public Page<PriceAlertResponseDto> findAllByUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid PriceAlertFilterDto filters,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        var userDetails = OAuthUserDetails.fromJwt(jwt);
        return priceAlertService.findAllByUser(userDetails, filters, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PriceAlertResponseDto createPriceAlert(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PriceAlertRequestDto alertDto) {
        return priceAlertService.createPriceAlert(OAuthUserDetails.fromJwt(jwt), alertDto.productId(), alertDto);
    }

    @DeleteMapping("/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePriceAlert(@AuthenticationPrincipal Jwt jwt, @ValidPriceAlertId @PathVariable String alertId) {
        priceAlertService.deletePriceAlert(OAuthUserDetails.fromJwt(jwt), alertId);
    }

    @DeleteMapping("/inactive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInactivePriceAlerts(@AuthenticationPrincipal Jwt jwt) {
        priceAlertService.deleteInactivePriceAlerts(OAuthUserDetails.fromJwt(jwt));
    }

    @PutMapping("/{alertId}")
    public PriceAlertResponseDto updatePriceAlert(
            @AuthenticationPrincipal Jwt jwt,
            @ValidPriceAlertId @PathVariable String alertId,
            @Valid @RequestBody PriceAlertRequestDto alertDto) {
        return priceAlertService.updatePriceAlert(OAuthUserDetails.fromJwt(jwt), alertId, alertDto);
    }
}
