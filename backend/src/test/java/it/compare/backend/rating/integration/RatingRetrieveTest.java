package it.compare.backend.rating.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

class RatingRetrieveTest extends RatingTest {

    @Test
    void shouldReturnUnauthorized() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnEmptyResponseIfNoRatingExists() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", nullValue());

        assertThat(ratingRepository.count(), equalTo(0L));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnRatingIfExists(boolean isPositive) {
        ratingTestDataFactory.createRatingForComment(testComment, user, isPositive);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(isPositive));

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnOnlyCurrentUserRating() {
        var anotherUser = userTestDataFactory.createOne();
        ratingTestDataFactory.createRatingForComment(testComment, anotherUser, false);
        ratingTestDataFactory.createRatingForComment(testComment, user, true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(true));

        assertThat(ratingRepository.count(), equalTo(2L));
    }
}
