package it.compare.backend.favoriteproduct.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.product.model.BestOffer;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
                Instant.now().minus(Duration.ofDays(favoriteProducts.indexOf(favoriteProduct) + 1))));
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

    @Test
    void shouldReturnFavoriteProductsSortedByLowestCurrentPrice() {
        var user = userTestDataFactory.createOne();
        var favoriteProducts = favoriteProductTestDataFactory.createMany(3, user).stream()
                .sorted(Comparator.comparing(favoriteProduct -> Optional.ofNullable(
                                favoriteProduct.getProduct().getComputedState().getBestOffer())
                        .map(BestOffer::getPrice)
                        .orElse(BigDecimal.ZERO)))
                .toList();

        var mockToken = AuthMock.getToken(user.getId(), user.getUsername(), user.getEmail(), List.of());
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .param("sort", "lowestCurrentPrice")
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
