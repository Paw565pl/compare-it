package it.compare.backend.comment.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.model.Role;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.auth.util.AuthUtil;
import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.comment.mapper.CommentMapper;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.comment.repository.CommentRepository;
import it.compare.backend.comment.response.CommentResponse;
import it.compare.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ProductService productService;
    private final UserRepository userRepository;

    public Comment findCommentOrThrow(String id) {
        return commentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    public Page<CommentResponse> findAll(String productId, Pageable pageable) {
        productService.findProductOrThrow(productId);
        var comments = commentRepository.findAllByProductId(productId, pageable);

        return comments.map(commentMapper::toResponse);
    }

    public CommentResponse findById(String productId, String commentId) {
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        return commentMapper.toResponse(comment);
    }

    @Transactional
    public CommentResponse create(OAuthUserDetails oAuthUserDetails, String productId, CommentDto commentDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        try {
            var comment = commentMapper.toEntity(commentDto);
            comment.setAuthor(user);
            comment.setProduct(product);
            var savedComment = commentRepository.save(comment);

            return commentMapper.toResponse(savedComment);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("You have already commented on this product.");
        }
    }

    @Transactional
    public void deleteById(OAuthUserDetails oAuthUserDetails, String productId, String commentId) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        productService.findProductOrThrow(productId);
        var comment = findCommentOrThrow(commentId);

        var canDelete = user.getId().equals(comment.getAuthor().getId())
                || AuthUtil.hasRole(oAuthUserDetails.getAuthorities(), Role.ADMIN);
        if (!canDelete) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        commentRepository.deleteById(commentId);
    }
}
