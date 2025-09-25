package it.compare.backend.pricealert.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;

import it.compare.backend.auth.model.User;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.service.EmailService;
import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PriceAlertIntegrationTest extends PriceAlertTest {

    @Autowired
    private PriceAlertService priceAlertService;

    @MockitoBean
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = userTestDataFactory.createOne();
    }

    @Test
    void shouldSendEmailAndDeactivateAlertWhenPriceReachesTheTarget() {
        var lowPrice = new PriceStamp(BigDecimal.valueOf(90), Currency.PLN, Condition.NEW);
        lowPrice.setTimestamp(Instant.now());

        var offer = new Offer(Shop.RTV_EURO_AGD, "https://example.com/product", lowPrice);

        var product = productTestDataFactory.createOne();
        product.getOffers().add(offer);

        var alert = new PriceAlert(testUser, product, BigDecimal.valueOf(100), true);
        alert.setIsActive(true);
        alert.setCreatedAt(Instant.now());

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
