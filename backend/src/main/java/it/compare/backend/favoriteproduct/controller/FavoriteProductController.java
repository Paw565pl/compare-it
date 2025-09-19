package it.compare.backend.favoriteproduct.controller;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.favoriteproduct.dto.FavoriteProductRequestDto;
import it.compare.backend.favoriteproduct.dto.FavoriteProductStatusResponseDto;
import it.compare.backend.favoriteproduct.service.FavoriteProductService;
import it.compare.backend.product.dto.ProductListResponseDto;
import it.compare.backend.product.validator.ValidProductId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@IsAuthenticated
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorite-products")
public class FavoriteProductController {

    private final FavoriteProductService favoriteProductService;

    @GetMapping
    public Page<ProductListResponseDto> findAllByUser(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        return favoriteProductService.findAllByUser(OAuthUserDetails.fromJwt(jwt), pageable);
    }

    @GetMapping("/{productId}/status")
    public FavoriteProductStatusResponseDto findFavoriteProductStatus(
            @AuthenticationPrincipal Jwt jwt, @ValidProductId @PathVariable String productId) {
        return favoriteProductService.findFavoriteProductStatus(OAuthUserDetails.fromJwt(jwt), productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addFavoriteProduct(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FavoriteProductRequestDto favoriteProductRequestDto) {
        favoriteProductService.addFavoriteProduct(OAuthUserDetails.fromJwt(jwt), favoriteProductRequestDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavoriteProduct(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody FavoriteProductRequestDto favoriteProductRequestDto) {
        favoriteProductService.removeFavoriteProduct(OAuthUserDetails.fromJwt(jwt), favoriteProductRequestDto);
    }
}
