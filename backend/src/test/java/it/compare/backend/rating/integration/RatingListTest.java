package it.compare.backend.rating.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RatingListTest extends RatingTest {

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

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

    @Test
    void shouldReturnPositiveRatingIfExists() {
        ratingTestDataFactory.createRatingForComment(testComment, user, true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(true));

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNegativeRatingIfExists() {
        ratingTestDataFactory.createRatingForComment(testComment, user, false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(false));

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
