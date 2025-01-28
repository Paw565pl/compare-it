package it.compare.backend.favoriteproduct.repository;

import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FavoriteProductRepository extends MongoRepository<FavoriteProduct, String> {
    Optional<FavoriteProduct> findByUserIdAndProductId(String userId, String productId);
}
