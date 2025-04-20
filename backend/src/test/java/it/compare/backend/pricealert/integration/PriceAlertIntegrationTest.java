package it.compare.backend.pricealert.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;

import it.compare.backend.auth.model.User;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.service.EmailService;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Offer;
import it.compare.backend.product.model.PriceStamp;
import it.compare.backend.product.model.Shop;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PriceAlertIntegrationTest extends PriceAlertTest {

    @Autowired
    private PriceAlertService priceAlertService;

    @Autowired
    private UserTestDataFactory userFactory;

    @Autowired
    private ProductTestDataFactory productFactory;

    @MockitoBean
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userFactory.createOne();
    }

    @Test
    void shouldSendEmailAndDeactivateAlertWhenPriceReachesTheTarget() {
        var product = productFactory.createOne();

        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product");
        var lowPrice = new PriceStamp(BigDecimal.valueOf(90), "PLN", Condition.NEW);
        lowPrice.setTimestamp(LocalDateTime.now());
        offer.getPriceHistory().add(lowPrice);
        product.getOffers().add(offer);

        var alert = new PriceAlert(product, BigDecimal.valueOf(100));
        alert.setUser(testUser);
        alert.setIsOutletAllowed(true);
        alert.setIsActive(true);
        alert.setCreatedAt(LocalDateTime.now());

        priceAlertRepository.save(alert);

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

        var updatedAlert = priceAlertRepository.findById(alert.getId()).orElseThrow();
        assertThat(updatedAlert.getIsActive(), is(false));
        assertThat(updatedAlert.getLastNotificationSent(), is(notNullValue()));
    }
}
