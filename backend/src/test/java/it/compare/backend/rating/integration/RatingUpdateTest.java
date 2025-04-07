package it.compare.backend.rating.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.rating.dto.RatingDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RatingUpdateTest extends RatingTest {

    @BeforeEach
    void setUpUpdateTest() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
        ratingTestDataFactory.createRatingForComment(testComment, user, true);
    }

    @Test
    void shouldReturnUnauthorized() {
        var body = new RatingDto(false);

        given().contentType(JSON)
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var body = new RatingDto(false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        var body = new RatingDto(false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfRatingDoesNotExist() {
        var newComment = commentTestDataFactory.createCommentForProduct(testProduct);
        var body = new RatingDto(false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), newComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldSuccessfullyUpdateRatingFromPositiveToNegative() {
        var body = new RatingDto(false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(false));

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldSuccessfullyUpdateRatingToSameValue() {
        var body = new RatingDto(true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isPositive", equalTo(true));

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnBadRequestIfIsPositiveIsNull() {
        var body = new RatingDto(null);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .put("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }
}
