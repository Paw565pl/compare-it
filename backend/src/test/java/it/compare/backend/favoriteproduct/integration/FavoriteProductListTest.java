package it.compare.backend.favoriteproduct.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.core.mock.AuthMock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FavoriteProductListTest extends FavoriteProductTest {

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnUnauthorized() {
        given().contentType(JSON).when().get().then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnEmptyList() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnUsersFavoriteProducts() {
        var favoriteProduct = favoriteProductTestDataFactory.createOne();
        var favoriteProductUserId = favoriteProduct.getUser().getId();
        var favoriteProductUsername = favoriteProduct.getUser().getUsername();
        var favoriteProductEmail = favoriteProduct.getUser().getEmail();
        var userMockToken =
                AuthMock.getToken(favoriteProductUserId, favoriteProductUsername, favoriteProductEmail, List.of());

        var otherUser = userTestDataFactory.createOne();
        var otherUserMockToken =
                AuthMock.getToken(otherUser.getId(), otherUser.getUsername(), otherUser.getEmail(), List.of());

        when(jwtDecoder.decode(userMockToken.getTokenValue())).thenReturn(userMockToken);
        given().contentType(JSON)
                .auth()
                .oauth2(userMockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(1));

        when(jwtDecoder.decode(otherUserMockToken.getTokenValue())).thenReturn(otherUserMockToken);
        given().contentType(JSON)
                .auth()
                .oauth2(otherUserMockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnFavoriteProductsSortedByCreatedAt() {
        var user = userTestDataFactory.createOne();
        var favoriteProducts = favoriteProductTestDataFactory.createMany(3, user);

        favoriteProducts.forEach(favoriteProduct -> favoriteProduct.setCreatedAt(
                LocalDateTime.now().minusDays(favoriteProducts.indexOf(favoriteProduct) + 1)));
        favoriteProductRepository.saveAll(favoriteProducts);

        var mockToken = AuthMock.getToken(user.getId(), user.getUsername(), user.getEmail(), List.of());
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .param("sort", "createdAt,asc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        "content[0].id",
                        equalTo(favoriteProducts.get(2).getProduct().getId()))
                .body(
                        "content[1].id",
                        equalTo(favoriteProducts.get(1).getProduct().getId()))
                .body(
                        "content[2].id",
                        equalTo(favoriteProducts.get(0).getProduct().getId()));

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .param("sort", "createdAt,desc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        "content[0].id",
                        equalTo(favoriteProducts.get(0).getProduct().getId()))
                .body(
                        "content[1].id",
                        equalTo(favoriteProducts.get(1).getProduct().getId()))
                .body(
                        "content[2].id",
                        equalTo(favoriteProducts.get(2).getProduct().getId()));
    }
}
