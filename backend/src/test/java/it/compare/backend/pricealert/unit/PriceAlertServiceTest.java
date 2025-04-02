package it.compare.backend.pricealert.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.compare.backend.auth.model.User;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.respository.PriceAlertRepository;
import it.compare.backend.pricealert.service.EmailService;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private PriceAlertService priceAlertService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(String.valueOf(UUID.randomUUID()));
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        var testProduct = createTestProduct();

        var testAlert = new PriceAlert(testProduct, BigDecimal.valueOf(100));
        testAlert.setId(String.valueOf(UUID.randomUUID()));
        testAlert.setUser(testUser);
        testAlert.setIsOutletAllowed(true);
    }

    private Product createTestProduct() {
        var product = new Product(String.valueOf(UUID.randomUUID()), "Test Product", Category.PROCESSORS);

        var priceStamp = new PriceStamp(BigDecimal.valueOf(150), "PLN", Condition.NEW);
        priceStamp.setTimestamp(LocalDateTime.now());

        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product");
        offer.getPriceHistory().add(priceStamp);

        product.getOffers().add(offer);

        return product;
    }

    @Test
    void shouldSendEmailWhenPriceBelowTarget() {
        var product = createTestProduct();
        product.getOffers().clear();

        var lowPriceStamp = new PriceStamp(BigDecimal.valueOf(90), "PLN", Condition.NEW);
        lowPriceStamp.setTimestamp(LocalDateTime.now());

        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product");
        offer.getPriceHistory().add(lowPriceStamp);

        product.getOffers().add(offer);

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(true);
        alert.setActive(true);
        alert.setCreatedAt(LocalDateTime.now());

        var alerts = List.of(alert);

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(alerts);

        priceAlertService.checkPriceAlerts(product);

        verify(emailService)
                .sendPriceAlert(
                        testUser.getEmail(),
                        product.getName(),
                        product.getId(),
                        BigDecimal.valueOf(90),
                        BigDecimal.valueOf(100),
                        Shop.RTV_EURO_AGD.getHumanReadableName(),
                        "https://example.com/product");

        var alertCaptor = ArgumentCaptor.forClass(PriceAlert.class);
        verify(priceAlertRepository).save(alertCaptor.capture());

        var savedAlert = alertCaptor.getValue();
        assertFalse(savedAlert.getActive());
        assertNotNull(savedAlert.getLastNotificationSent());
    }

    @Test
    void shouldNotSendEmailWhenPriceAboveTarget() {
        var product = createTestProduct();

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(true);
        alert.setActive(true);

        var alerts = List.of(alert);

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(alerts);

        priceAlertService.checkPriceAlerts(product);

        verifyNoInteractions(emailService);
        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
        assertTrue(alert.getActive());
    }

    @Test
    void shouldRespectOutletAllowedWhenCheckingPrices() {
        var product = new Product(String.valueOf(UUID.randomUUID()), "Test Product", Category.PROCESSORS);

        var outletPriceStamp = new PriceStamp(BigDecimal.valueOf(80), "PLN", Condition.OUTLET);
        outletPriceStamp.setTimestamp(LocalDateTime.now());

        var outletOffer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/outlet");
        outletOffer.getPriceHistory().add(outletPriceStamp);

        var newPriceStamp = new PriceStamp(BigDecimal.valueOf(120), "PLN", Condition.NEW);
        newPriceStamp.setTimestamp(LocalDateTime.now());

        var newOffer = new Offer(Shop.MEDIA_EXPERT, "https://example.com/new");
        newOffer.getPriceHistory().add(newPriceStamp);

        product.getOffers().add(outletOffer);
        product.getOffers().add(newOffer);

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(false);
        alert.setActive(true);
        alert.setCreatedAt(LocalDateTime.now());

        var alerts = List.of(alert);

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(alerts);

        priceAlertService.checkPriceAlerts(product);

        verifyNoInteractions(emailService);
        verify(priceAlertRepository, never()).save(any(PriceAlert.class));

        alert.setIsOutletAllowed(true);

        priceAlertService.checkPriceAlerts(product);

        verify(emailService)
                .sendPriceAlert(
                        testUser.getEmail(),
                        product.getName(),
                        product.getId(),
                        BigDecimal.valueOf(80),
                        BigDecimal.valueOf(100),
                        Shop.RTV_EURO_AGD.getHumanReadableName(),
                        "https://example.com/outlet");
    }
}
