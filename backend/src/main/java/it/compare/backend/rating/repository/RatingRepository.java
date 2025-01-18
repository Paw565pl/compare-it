package it.compare.backend.rating.repository;

import it.compare.backend.rating.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RatingRepository extends MongoRepository<Rating, String> {}
