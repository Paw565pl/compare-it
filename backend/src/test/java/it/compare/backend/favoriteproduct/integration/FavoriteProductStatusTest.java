package it.compare.backend.favoriteproduct.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

@Import(ProductTestDataFactory.class)
class FavoriteProductStatusTest extends FavoriteProductTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    private static final Jwt mockToken = AuthMock.getToken("1", List.of());

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnUnauthorized() {
        var productId = productTestDataFactory.createOne().getId();

        given().contentType(JSON)
                .when()
                .get("/{productId}/status", productId)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/status", new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnCorrectStatusIfProductIsNotFavorite() {
        var product = productTestDataFactory.createOne();

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/status", product.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isFavorite", equalTo(false));
    }

    @Test
    void shouldReturnCorrectStatusIfProductIsFavorite() {
        var favoriteProduct = favoriteProductTestDataFactory.createOne();

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/status", favoriteProduct.getProduct().getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isFavorite", equalTo(true));
    }
}
