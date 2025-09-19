package it.compare.backend.rating.controller;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.comment.validator.ValidCommentId;
import it.compare.backend.product.validator.ValidProductId;
import it.compare.backend.rating.dto.RatingRequestDto;
import it.compare.backend.rating.dto.RatingResponseDto;
import it.compare.backend.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@IsAuthenticated
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/comments/{commentId}/rate")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public RatingResponseDto findByAuthorIdAndCommentId(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId) {
        return ratingService.findByAuthorIdAndCommentId(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingResponseDto create(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId,
            @Valid @RequestBody RatingRequestDto ratingRequestDto) {
        return ratingService.create(OAuthUserDetails.fromJwt(jwt), productId, commentId, ratingRequestDto);
    }

    @PutMapping
    public RatingResponseDto update(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId,
            @Valid @RequestBody RatingRequestDto ratingRequestDto) {
        return ratingService.update(OAuthUserDetails.fromJwt(jwt), productId, commentId, ratingRequestDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAuthorIdAndCommentId(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId) {
        ratingService.deleteByAuthorIdAndCommentId(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }
}
