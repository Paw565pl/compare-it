package it.compare.backend.favoriteproduct.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.mock.AuthMock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class FavoriteProductListTest extends FavoriteProductTest {

    private static final Jwt mockToken = AuthMock.getToken("1", List.of());

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnUnauthorized() {
        given().contentType(JSON).when().get().then().statusCode(401);
    }

    @Test
    void shouldReturnEmptyList() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnUsersFavoriteProducts() {
        var favoriteProduct = favoriteProductTestDataFactory.createOne();
        var favoriteProductUserId = favoriteProduct.getUser().getId();
        var userMockToken = AuthMock.getToken(favoriteProductUserId, List.of());

        // TODO: change this to use user data factory
        var otherUser = new User("2", "test", "test@test.com");
        userRepository.save(otherUser);
        var otherUserMockToken = AuthMock.getToken(otherUser.getId(), List.of());

        when(jwtDecoder.decode(userMockToken.getTokenValue())).thenReturn(userMockToken);
        given().contentType(JSON)
                .auth()
                .oauth2(userMockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(1));

        when(jwtDecoder.decode(otherUserMockToken.getTokenValue())).thenReturn(otherUserMockToken);
        given().contentType(JSON)
                .auth()
                .oauth2(otherUserMockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnFavoriteProductsSortedByCreatedAt() {
        var favoriteProducts =
                favoriteProductTestDataFactory.createMany(3).stream().toList();
        favoriteProducts.forEach(favoriteProduct -> favoriteProduct.setCreatedAt(
                LocalDateTime.now().minusDays(favoriteProducts.indexOf(favoriteProduct) + 1)));
        favoriteProductRepository.saveAll(favoriteProducts);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .param("sort", "createdAt,asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .log()
                .all()
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
                .statusCode(200)
                .log()
                .all()
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
