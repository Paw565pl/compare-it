package it.compare.backend.comment.repository;

import it.compare.backend.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
    Page<Comment> findAllByProductId(String productId, Pageable pageable);
}
