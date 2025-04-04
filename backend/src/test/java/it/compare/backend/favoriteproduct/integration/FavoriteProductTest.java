package it.compare.backend.favoriteproduct.integration;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.favoriteproduct.datafactory.FavoriteProductTestDataFactory;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({UserTestDataFactory.class, FavoriteProductTestDataFactory.class})
abstract class FavoriteProductTest extends IntegrationTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    UserTestDataFactory userTestDataFactory;

    @Autowired
    FavoriteProductTestDataFactory favoriteProductTestDataFactory;

    @Autowired
    FavoriteProductRepository favoriteProductRepository;

    User user;
    Jwt mockToken;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/favorite-products");

        user = userTestDataFactory.createOne();
        mockToken = AuthMock.getToken(user.getId(), List.of());
    }

    @AfterEach
    void tearDown() {
        favoriteProductTestDataFactory.clear();
    }
}
