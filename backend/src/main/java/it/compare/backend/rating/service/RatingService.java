package it.compare.backend.rating.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.comment.service.CommentService;
import it.compare.backend.product.service.ProductService;
import it.compare.backend.rating.dto.RatingRequestDto;
import it.compare.backend.rating.dto.RatingResponseDto;
import it.compare.backend.rating.mapper.RatingMapper;
import it.compare.backend.rating.model.Rating;
import it.compare.backend.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final ProductService productService;
    private final CommentService commentService;
    private final UserRepository userRepository;

    public Rating findRatingOrThrow(String authorId, String commentId) {
        return ratingRepository
                .findByAuthorIdAndCommentId(authorId, commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found."));
    }

    public RatingResponseDto findByAuthorIdAndCommentId(
            OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        commentService.findCommentOrThrow(commentId);

        var rating =
                ratingRepository.findByAuthorIdAndCommentId(userId, commentId).orElse(new Rating());
        return ratingMapper.toResponseDto(rating);
    }

    @Transactional
    public RatingResponseDto create(
            OAuthUserDetails oAuthUserDetails, String productId, String commentId, RatingRequestDto ratingRequestDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        productService.findProductOrThrow(productId);
        var comment = commentService.findCommentOrThrow(commentId);

        try {
            var rating = ratingMapper.toEntity(ratingRequestDto);
            rating.setAuthor(user);
            rating.setComment(comment);

            var savedRating = ratingRepository.save(rating);
            return ratingMapper.toResponseDto(savedRating);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("You have already rated this comment.");
        }
    }

    @Transactional
    public RatingResponseDto update(
            OAuthUserDetails oAuthUserDetails, String productId, String commentId, RatingRequestDto ratingRequestDto) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        commentService.findCommentOrThrow(commentId);

        var rating = findRatingOrThrow(userId, commentId);
        rating.setIsPositive(ratingRequestDto.isPositive());

        var savedRating = ratingRepository.save(rating);
        return ratingMapper.toResponseDto(savedRating);
    }

    @Transactional
    public void deleteByAuthorIdAndCommentId(OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var userId = oAuthUserDetails.getId();
        productService.findProductOrThrow(productId);
        commentService.findCommentOrThrow(commentId);

        var rating = findRatingOrThrow(userId, commentId);
        ratingRepository.deleteById(rating.getId());
    }
}
