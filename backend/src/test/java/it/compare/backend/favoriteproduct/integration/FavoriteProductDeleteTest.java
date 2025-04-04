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

@Import(ProductTestDataFactory.class)
class FavoriteProductDeleteTest extends FavoriteProductTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnUnauthorized() {
        var body = new FavoriteProductDto(new ObjectId().toString());

        given().contentType(JSON).body(body).when().delete().then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var body = new FavoriteProductDto(new ObjectId().toString());

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .delete()
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnBadRequestIfProductIsNotFavorite() {
        var product = productTestDataFactory.createOne();
        var body = new FavoriteProductDto(product.getId());

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .delete()
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldDeleteFavoriteProduct() {
        var favoriteProduct = favoriteProductTestDataFactory.createOne();
        var body = new FavoriteProductDto(favoriteProduct.getProduct().getId());

        var mockToken = AuthMock.getToken(favoriteProduct.getUser().getId(), List.of());
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .delete()
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        assertThat(favoriteProductRepository.count(), equalTo(0L));
    }
}
