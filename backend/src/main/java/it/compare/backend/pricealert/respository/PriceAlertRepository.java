package it.compare.backend.pricealert.respository;

import it.compare.backend.pricealert.model.PriceAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PriceAlertRepository extends MongoRepository<PriceAlert, String> {
    boolean existsByUserIdAndProductIdAndActiveTrue(String userId, String productId);

    Page<PriceAlert> findAllByUserId(String userId, Pageable pageable);

    Page<PriceAlert> findAllByUserIdAndActive(String userId, boolean active, Pageable pageable);
}
