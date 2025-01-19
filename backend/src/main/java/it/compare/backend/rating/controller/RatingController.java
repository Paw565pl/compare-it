package it.compare.backend.rating.controller;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.rating.dto.RatingDto;
import it.compare.backend.rating.response.RatingResponse;
import it.compare.backend.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@IsAuthenticated
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/comments/{commentId}/rate")
public class RatingController {
    private final RatingService ratingService;

    @GetMapping
    public RatingResponse find(
            @AuthenticationPrincipal Jwt jwt, @PathVariable String productId, @PathVariable String commentId) {
        return ratingService.find(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @PathVariable String commentId,
            @Valid @RequestBody RatingDto ratingDto) {
        return ratingService.create(OAuthUserDetails.fromJwt(jwt), productId, commentId, ratingDto);
    }

    @PutMapping
    public RatingResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @PathVariable String commentId,
            @Valid @RequestBody RatingDto ratingDto) {
        return ratingService.update(OAuthUserDetails.fromJwt(jwt), productId, commentId, ratingDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @AuthenticationPrincipal Jwt jwt, @PathVariable String productId, @PathVariable String commentId) {
        ratingService.deleteById(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }
}
