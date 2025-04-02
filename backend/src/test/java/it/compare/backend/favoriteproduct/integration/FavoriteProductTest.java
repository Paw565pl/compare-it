package it.compare.backend.favoriteproduct.integration;

import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.favoriteproduct.datafactory.FavoriteProductTestDataFactory;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(FavoriteProductTestDataFactory.class)
abstract class FavoriteProductTest extends IntegrationTest {

    @Autowired
    FavoriteProductTestDataFactory favoriteProductTestDataFactory;

    @Autowired
    FavoriteProductRepository favoriteProductRepository;

    @Autowired
    UserRepository userRepository; // TODO: remove this and use user test data factory

    @MockitoBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/favorite-products");
    }

    @AfterEach
    void tearDown() {
        favoriteProductTestDataFactory.clear();
    }
}
