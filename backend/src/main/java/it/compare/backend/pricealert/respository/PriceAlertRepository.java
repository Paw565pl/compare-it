package it.compare.backend.pricealert.respository;

import it.compare.backend.pricealert.model.PriceAlert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceAlertRepository extends MongoRepository<PriceAlert, String> {
    boolean existsByUserIdAndProductIdAndIsActiveTrue(String userId, String productId);
}
