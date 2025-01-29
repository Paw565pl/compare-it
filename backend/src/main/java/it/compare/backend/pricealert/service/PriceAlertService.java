package it.compare.backend.pricealert.service;

import it.compare.backend.pricealert.dto.PriceAlertDto;
import it.compare.backend.pricealert.mapper.PriceAlertMapper;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.respository.PriceAlertRepository;
import it.compare.backend.pricealert.response.PriceAlertResponse;
import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.product.model.PriceStamp;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

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
        return priceAlertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert not found"));
    }

    public Page<PriceAlertResponse> findAllByUser(OAuthUserDetails userDetails, Pageable pageable) {
        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return priceAlertRepository.findAllByUserId(user.getId(), pageable)
                .map(priceAlertMapper::toResponse);
    }

    public Page<PriceAlertResponse> findAllByUserAndActive(OAuthUserDetails userDetails, boolean isActive, Pageable pageable) {
        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return priceAlertRepository.findAllByUserIdAndIsActive(user.getId(), isActive, pageable)
                .map(priceAlertMapper::toResponse);
    }

    @Transactional
    public PriceAlertResponse createPriceAlert(OAuthUserDetails userDetails, String productId, PriceAlertDto alertDto) {
        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(productId);

        if (priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(user.getId(), productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Active alert already exists for this product");
        }

        var alert = new PriceAlert(product, alertDto.targetPrice());
        alert.setUser(user);

        var savedAlert = priceAlertRepository.save(alert);
        return priceAlertMapper.toResponse(savedAlert);
    }

    @Transactional
    public void deletePriceAlert(OAuthUserDetails userDetails, String alertId) {
        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var alert = findAlertOrThrow(alertId);

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        priceAlertRepository.deleteById(alertId);
    }
    @Transactional
    public PriceAlertResponse updateTargetPrice(OAuthUserDetails userDetails, String alertId, PriceAlertDto alertDto) {
        var user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var alert = findAlertOrThrow(alertId);

        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        alert.setTargetPrice(alertDto.targetPrice());
        var savedAlert = priceAlertRepository.save(alert);

        return priceAlertMapper.toResponse(savedAlert);
    }

    public void checkPriceAlerts(Product product) {
        Query query = Query.query(
                new Criteria().andOperator(
                        Criteria.where("product.$id").is(product.getId()),
                        Criteria.where("isActive").is(true)
                )
        );

        List<PriceAlert> alerts = mongoTemplate.find(query, PriceAlert.class);

        var latestPrices = product.getOffers().stream()
                .filter(offer -> !offer.getPriceHistory().isEmpty())
                .map(offer -> {
                    var latestPrice = offer.getPriceHistory().stream()
                            .max(Comparator.comparing(PriceStamp::getTimestamp))
                            .orElse(null);
                    return new OfferPriceData(
                            offer.getShop().getHumanReadableName(),
                            latestPrice,
                            offer.getUrl()
                    );
                })
                .filter(latest -> latest.priceStamp() != null)
                .toList();

        var lowestPriceData = latestPrices.stream()
                .filter(latest -> latest.priceStamp().getIsAvailable())
                .min(Comparator.comparing(latest -> latest.priceStamp().getPrice()))
                .orElse(null);

        if (lowestPriceData == null) {
            return;
        }

        var lowestPrice = lowestPriceData.priceStamp().getPrice();

        alerts.forEach(alert -> {
            if (lowestPrice.compareTo(alert.getTargetPrice()) <= 0) {
                emailService.sendPriceAlert(
                        alert.getUser().getEmail(),
                        product.getName(),
                        product.getId(),
                        lowestPrice,
                        alert.getTargetPrice(),
                        lowestPriceData.shop(),
                        lowestPriceData.url()
                );

                alert.setLastNotificationSent(alert.getCreatedAt());
                alert.setActive(false);
                priceAlertRepository.save(alert);
            }
        });
    }

    private record OfferPriceData(String shop, PriceStamp priceStamp, String url) {}
}
