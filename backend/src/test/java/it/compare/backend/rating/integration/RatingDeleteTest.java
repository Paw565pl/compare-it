package it.compare.backend.rating.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RatingDeleteTest extends RatingTest {

    @BeforeEach
    void setUpDeleteTest() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
        ratingTestDataFactory.createRatingForComment(testComment, user, true);
    }

    @Test
    void shouldReturnUnauthorized() {
        given().contentType(JSON)
                .when()
                .delete("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}/rate", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}/rate", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfRatingDoesNotExist() {
        var newComment = commentTestDataFactory.createCommentForProduct(testProduct);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}/rate", testProduct.getId(), newComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(ratingRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNoContentAfterDeletingRating() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}/rate", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(ratingRepository.count(), equalTo(0L));
    }
}
