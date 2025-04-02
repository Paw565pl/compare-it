package it.compare.backend.favoriteproduct.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.favoriteproduct.dto.FavoriteProductDto;
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
class FavoriteProductCreateTest extends FavoriteProductTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    private static final Jwt mockToken = AuthMock.getToken("1", List.of());

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnUnauthorized() {
        var body = new FavoriteProductDto(new ObjectId().toString());

        given().contentType(JSON).body(body).when().post().then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var body = new FavoriteProductDto(new ObjectId().toString());

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldCreateFavoriteProduct() {
        var product = productTestDataFactory.createOne();
        var body = new FavoriteProductDto(product.getId());

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value());
        assertThat(favoriteProductRepository.count(), equalTo(1L));
    }
}
