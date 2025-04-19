package it.compare.backend.pricealert.respository;

import it.compare.backend.pricealert.model.PriceAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceAlertRepository extends MongoRepository<PriceAlert, String> {
    boolean existsByUserIdAndProductIdAndIsActiveTrue(String userId, String productId);

    Page<PriceAlert> findAllByUserId(String userId, Pageable pageable);

    Page<PriceAlert> findAllByUserIdAndProductId(String userId, String productId, Pageable pageable);

    Page<PriceAlert> findAllByUserIdAndIsActive(String userId, boolean isActive, Pageable pageable);

    Page<PriceAlert> findAllByUserIdAndProductIdAndIsActive(
            String userId, String productId, boolean isActive, Pageable pageable);

    void deleteAllByUserIdAndIsActiveFalse(String userId);
}
