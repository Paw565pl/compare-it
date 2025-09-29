package it.compare.backend.rating.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.rating.dto.RatingRequestDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

class RatingCreateTest extends RatingTest {

    @Test
    void shouldReturnUnauthorized() {
        var body = new RatingRequestDto(true);

        given().contentType(JSON)
                .body(body)
                .when()
                .post("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var body = new RatingRequestDto(true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments/{commentId}/rate", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        var body = new RatingRequestDto(true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments/{commentId}/rate", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnBadRequestIfIsPositiveIsNull() {
        var body = new RatingRequestDto(null);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCreatedAfterCreatingRating(boolean isPositive) {
        var body = new RatingRequestDto(isPositive);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("isPositive", equalTo(isPositive));

        assertThat(ratingRepository.count(), equalTo(1L));
    }
}
