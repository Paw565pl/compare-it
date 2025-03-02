package it.compare.backend.product.integration;

import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(ProductTestDataFactory.class)
abstract class ProductTest extends IntegrationTest {

    @Autowired
    ProductTestDataFactory productTestDataFactory;

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/products");
    }

    @AfterEach
    void tearDown() {
        productTestDataFactory.clear();
    }
}
