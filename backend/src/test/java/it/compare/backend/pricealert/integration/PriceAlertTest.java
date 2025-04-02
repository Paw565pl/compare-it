package it.compare.backend.pricealert.integration;

import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.pricealert.datafactory.PriceAlertTestDataFactory;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({PriceAlertTestDataFactory.class, ProductTestDataFactory.class, UserTestDataFactory.class})
abstract class PriceAlertTest extends IntegrationTest {

    @Autowired
    PriceAlertTestDataFactory priceAlertTestDataFactory;

    @Autowired
    ProductTestDataFactory productTestDataFactory;

    @Autowired
    UserTestDataFactory userTestDataFactory;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/price-alerts");
    }

    @AfterEach
    void tearDown() {
        priceAlertTestDataFactory.clear();
        userTestDataFactory.clear();
        productTestDataFactory.clear();
    }
}
