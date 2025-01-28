package it.compare.backend.rating.repository;

import it.compare.backend.rating.model.Rating;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingRepository extends MongoRepository<Rating, String> {
    Optional<Rating> findByAuthorIdAndCommentId(String authorId, String commentId);

    void deleteAllByCommentId(String commentId);
}
