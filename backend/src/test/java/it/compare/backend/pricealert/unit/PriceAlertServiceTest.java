package it.compare.backend.pricealert.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.compare.backend.auth.model.User;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.respository.PriceAlertRepository;
import it.compare.backend.pricealert.service.EmailService;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.*;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import net.datafaker.Faker;
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

    private final Faker faker = new Faker();
    private final UserTestDataFactory userFactory = new UserTestDataFactory(faker, null);
    private final ProductTestDataFactory productFactory = new ProductTestDataFactory(faker, null);

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userFactory.generate();
    }

    @Test
    void shouldSendEmailWhenPriceBelowTarget() {
        var product = productFactory.generate();

        product.getOffers().clear();
        var lowPriceStamp = new PriceStamp(BigDecimal.valueOf(90), "PLN", Condition.NEW);
        lowPriceStamp.setTimestamp(LocalDateTime.now());
        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product");
        offer.getPriceHistory().add(lowPriceStamp);
        product.getOffers().add(offer);

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(true);
        alert.setIsActive(true);
        alert.setCreatedAt(LocalDateTime.now());

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(List.of(alert));

        priceAlertService.checkPriceAlerts(List.of(product));

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
        assertThat(savedAlert.getIsActive(), is(false));
        assertThat(savedAlert.getLastNotificationSent(), notNullValue());
    }

    @Test
    void shouldNotSendEmailWhenPriceAboveTarget() {
        var product = productFactory.generate();

        product.getOffers().clear();
        var highPriceStamp = new PriceStamp(BigDecimal.valueOf(150), "PLN", Condition.NEW);
        highPriceStamp.setTimestamp(LocalDateTime.now());
        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product");
        offer.getPriceHistory().add(highPriceStamp);
        product.getOffers().add(offer);

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(true);
        alert.setIsActive(true);

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(List.of(alert));
        priceAlertService.checkPriceAlerts(List.of(product));

        verifyNoInteractions(emailService);
        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
        assertThat(alert.getIsActive(), is(true));
    }

    @Test
    void shouldRespectOutletAllowedWhenCheckingPrices() {
        var product = createProductWithNewAndOutletOffers(BigDecimal.valueOf(120), BigDecimal.valueOf(80));

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(false);
        alert.setIsActive(true);
        alert.setCreatedAt(LocalDateTime.now());

        when(mongoTemplate.find(any(Query.class), eq(PriceAlert.class))).thenReturn(List.of(alert));

        priceAlertService.checkPriceAlerts(List.of(product));

        verifyNoInteractions(emailService);
        verify(priceAlertRepository, never()).save(any(PriceAlert.class));

        alert.setIsOutletAllowed(true);
        priceAlertService.checkPriceAlerts(List.of(product));

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

    private Product createProductWithNewAndOutletOffers(BigDecimal newPrice, BigDecimal outletPrice) {

        var product = productFactory.generate();
        product.getOffers().clear();

        var outletPriceStamp = new PriceStamp(outletPrice, "PLN", Condition.OUTLET);
        outletPriceStamp.setTimestamp(LocalDateTime.now());
        var outletOffer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/outlet");
        outletOffer.getPriceHistory().add(outletPriceStamp);
        product.getOffers().add(outletOffer);

        var newPriceStamp = new PriceStamp(newPrice, "PLN", Condition.NEW);
        newPriceStamp.setTimestamp(LocalDateTime.now());
        var newOffer = new Offer(Shop.MEDIA_EXPERT, "https://example.com/new");
        newOffer.getPriceHistory().add(newPriceStamp);
        product.getOffers().add(newOffer);

        return product;
    }
}
