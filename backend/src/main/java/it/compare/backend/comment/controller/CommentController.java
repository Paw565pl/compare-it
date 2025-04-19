package it.compare.backend.comment.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.comment.response.CommentResponse;
import it.compare.backend.comment.service.CommentService;
import it.compare.backend.product.validator.ValidProductId;
import jakarta.validation.Valid;
import java.util.Optional;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public Page<CommentResponse> findAllByProductId(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        var oAuthUserDetails =
                Optional.ofNullable(jwt).map(OAuthUserDetails::fromJwt).orElse(null);
        return commentService.findAllByProductId(oAuthUserDetails, productId, pageable);
    }

    @GetMapping("/{commentId}")
    public CommentResponse findById(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @PathVariable String commentId) {
        var oAuthUserDetails =
                Optional.ofNullable(jwt).map(OAuthUserDetails::fromJwt).orElse(null);
        return commentService.findById(oAuthUserDetails, productId, commentId);
    }

    @IsAuthenticated
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @Valid @RequestBody CommentDto commentDto) {
        return commentService.create(OAuthUserDetails.fromJwt(jwt), productId, commentDto);
    }

    @IsAuthenticated
    @PutMapping("/{commentId}")
    public CommentResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @PathVariable String commentId,
            @Valid @RequestBody CommentDto commentDto) {
        return commentService.update(OAuthUserDetails.fromJwt(jwt), productId, commentId, commentDto);
    }

    @IsAuthenticated
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @PathVariable String commentId) {
        commentService.deleteById(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }
}
