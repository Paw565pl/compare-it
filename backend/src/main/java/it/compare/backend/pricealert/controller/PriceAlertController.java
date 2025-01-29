package it.compare.backend.pricealert.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.pricealert.dto.PriceAlertDto;
import it.compare.backend.pricealert.response.PriceAlertResponse;
import it.compare.backend.pricealert.service.PriceAlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @IsAuthenticated
    @GetMapping("/price-alerts")
    public Page<PriceAlertResponse> findAllByUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        if (active != null) {
            return priceAlertService.findAllByUserAndActive(OAuthUserDetails.fromJwt(jwt), active, pageable);
        }
        return priceAlertService.findAllByUser(OAuthUserDetails.fromJwt(jwt), pageable);
    }

    @IsAuthenticated
    @PostMapping("/price-alerts")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceAlertResponse createPriceAlert(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PriceAlertDto alertDto) {
        return priceAlertService.createPriceAlert(OAuthUserDetails.fromJwt(jwt), alertDto.productId(), alertDto);
    }

    @IsAuthenticated
    @DeleteMapping("/price-alerts/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePriceAlert(@AuthenticationPrincipal Jwt jwt, @PathVariable String alertId) {
        priceAlertService.deletePriceAlert(OAuthUserDetails.fromJwt(jwt), alertId);
    }

    @IsAuthenticated
    @PutMapping("/price-alerts/{alertId}")
    public PriceAlertResponse updatePriceAlert(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String alertId,
            @Valid @RequestBody PriceAlertDto alertDto) {
        return priceAlertService.updatePriceAlert(OAuthUserDetails.fromJwt(jwt), alertId, alertDto);
    }
}
