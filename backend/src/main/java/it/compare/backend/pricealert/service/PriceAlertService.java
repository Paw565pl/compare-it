package it.compare.backend.pricealert.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.pricealert.dto.PriceAlertFilterDto;
import it.compare.backend.pricealert.dto.PriceAlertRequestDto;
import it.compare.backend.pricealert.dto.PriceAlertResponseDto;
import it.compare.backend.pricealert.mapper.PriceAlertMapper;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.respository.PriceAlertRepository;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.service.ProductService;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final MongoTemplate mongoTemplate;
    private final ProductService productService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final PriceAlertMapper priceAlertMapper;

    public PriceAlert findAlertOrThrow(String id) {
        return priceAlertRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
    }

    public Page<PriceAlertResponseDto> findAllByUser(
            OAuthUserDetails userDetails, PriceAlertFilterDto filters, Pageable pageable) {
        var user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var userId = user.getId();
        Page<PriceAlert> alerts;

        if (filters.productId() != null && filters.isActive() != null)
            alerts = priceAlertRepository.findAllByUserIdAndProductIdAndIsActive(
                    userId, filters.productId(), filters.isActive(), pageable);
        else if (filters.productId() != null)
            alerts = priceAlertRepository.findAllByUserIdAndProductId(userId, filters.productId(), pageable);
        else if (filters.isActive() != null)
            alerts = priceAlertRepository.findAllByUserIdAndIsActive(userId, filters.isActive(), pageable);
        else alerts = priceAlertRepository.findAllByUserId(userId, pageable);

        return alerts.map(priceAlertMapper::toResponse);
    }

    @Transactional
    public PriceAlertResponseDto create(OAuthUserDetails userDetails, String productId, PriceAlertRequestDto alertDto) {
        var user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        if (priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(user.getId(), productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active alert already exists for this product");
        }

        var alert = new PriceAlert(user, product, alertDto.targetPrice(), alertDto.isOutletAllowed());

        var savedAlert = priceAlertRepository.save(alert);
        return priceAlertMapper.toResponse(savedAlert);
    }

    @Transactional
    public PriceAlertResponseDto update(OAuthUserDetails userDetails, String alertId, PriceAlertRequestDto alertDto) {
        var user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var alert = findAlertOrThrow(alertId);

        if (!alert.getUser().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        if (alert.getIsActive().equals(false))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can not update inactive alerts");

        alert.setTargetPrice(alertDto.targetPrice());
        alert.setIsOutletAllowed(alertDto.isOutletAllowed());
        var savedAlert = priceAlertRepository.save(alert);

        return priceAlertMapper.toResponse(savedAlert);
    }

    @Transactional
    public void deleteById(OAuthUserDetails userDetails, String alertId) {
        var user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var alert = findAlertOrThrow(alertId);

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        priceAlertRepository.deleteById(alertId);
    }

    @Transactional
    public void deleteInactive(OAuthUserDetails userDetails) {
        var user = userRepository
                .findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        priceAlertRepository.deleteAllByUserIdAndIsActiveFalse(user.getId());
    }

    public void checkPriceAlerts(List<Product> products) {
        var objectIds = products.stream().map(Product::getId).map(ObjectId::new).toList();

        var query = Query.query(new Criteria()
                .andOperator(
                        Criteria.where("product.$id").in(objectIds),
                        Criteria.where("isActive").is(true)));

        var alerts = mongoTemplate.find(query, PriceAlert.class);
        var alertsByProductId = alerts.stream()
                .collect(Collectors.groupingBy(alert -> alert.getProduct().getId()));

        for (var product : products) {
            var bestOffer = product.getComputedState().getBestOffer();
            var productAlerts = alertsByProductId.get(product.getId());

            if (bestOffer == null || productAlerts == null || productAlerts.isEmpty()) continue;

            for (var alert : productAlerts) {
                var bestOfferMeetsAlertCriteria = bestOffer.getPrice().compareTo(alert.getTargetPrice()) <= 0
                        && (alert.getIsOutletAllowed() || bestOffer.getCondition() != Condition.OUTLET);

                if (bestOfferMeetsAlertCriteria) {
                    emailService.sendPriceAlert(
                            alert.getUser().getEmail(),
                            product.getName(),
                            product.getId(),
                            alert.getTargetPrice(),
                            bestOffer.getPrice(),
                            bestOffer.getShop(),
                            bestOffer.getUrl());

                    alert.setLastNotificationSent(Instant.now());
                    alert.setIsActive(false);

                    priceAlertRepository.save(alert);
                }
            }
        }
    }
}
