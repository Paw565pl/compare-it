package it.compare.backend.comment.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import it.compare.backend.auth.annotation.IsAuthenticated;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.comment.dto.CommentRequestDto;
import it.compare.backend.comment.dto.CommentResponseDto;
import it.compare.backend.comment.service.CommentService;
import it.compare.backend.comment.validator.ValidCommentId;
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
    public Page<CommentResponseDto> findAllByProductId(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        var oAuthUserDetails =
                Optional.ofNullable(jwt).map(OAuthUserDetails::fromJwt).orElse(null);
        return commentService.findAllByProductId(oAuthUserDetails, productId, pageable);
    }

    @GetMapping("/{commentId}")
    public CommentResponseDto findById(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId) {
        var oAuthUserDetails =
                Optional.ofNullable(jwt).map(OAuthUserDetails::fromJwt).orElse(null);
        return commentService.findById(oAuthUserDetails, productId, commentId);
    }

    @IsAuthenticated
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto create(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.create(OAuthUserDetails.fromJwt(jwt), productId, commentRequestDto);
    }

    @IsAuthenticated
    @PutMapping("/{commentId}")
    public CommentResponseDto update(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.update(OAuthUserDetails.fromJwt(jwt), productId, commentId, commentRequestDto);
    }

    @IsAuthenticated
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(
            @AuthenticationPrincipal Jwt jwt,
            @ValidProductId @PathVariable String productId,
            @ValidCommentId @PathVariable String commentId) {
        commentService.deleteById(OAuthUserDetails.fromJwt(jwt), productId, commentId);
    }
}
