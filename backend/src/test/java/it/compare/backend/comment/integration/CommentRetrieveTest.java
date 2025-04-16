package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.comment.model.Comment;
import it.compare.backend.product.model.Product;
import it.compare.backend.rating.datafactory.RatingTestDataFactory;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import(RatingTestDataFactory.class)
class CommentRetrieveTest extends CommentTest {

    @Autowired
    private RatingTestDataFactory ratingTestDataFactory;

    private Product testProduct;
    private Comment testComment;

    @BeforeEach
    void setup() {
        testProduct = productTestDataFactory.createOne();
        testComment = commentTestDataFactory
                .createMultipleCommentsForProduct(testProduct, user, 1)
                .getFirst();
    }

    @Override
    @AfterEach
    void tearDown() {
        super.tearDown();
        ratingTestDataFactory.clear();
    }

    @Test
    void shouldReturnNotFoundForNonExistentComment() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnNotFoundForCommentForDifferentProduct() {
        var anotherProduct = productTestDataFactory.createOne();

        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", anotherProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnCommentById() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testComment.getId()))
                .body("text", equalTo(testComment.getText()))
                .body("author", equalTo(user.getUsername()));
    }

    @Test
    void shouldAlwaysReturnIsRatingPositiveFieldAsNullIfUserIsNotAuthenticated() {
        ratingTestDataFactory.createRatingForComment(testComment, user, true);

        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isRatingPositive", equalTo(null));
    }

    static Stream<Arguments> commentIsRatingPositiveFieldTestCases() {
        return Stream.of(Arguments.of(null, null), Arguments.of(true, true), Arguments.of(false, false));
    }

    @ParameterizedTest
    @MethodSource("commentIsRatingPositiveFieldTestCases")
    void shouldReturnCorrectIsRatingPositiveFieldValue(Boolean ratingValue, Boolean expectedIsRatingPositive) {
        if (ratingValue != null) ratingTestDataFactory.createRatingForComment(testComment, user, ratingValue);

        given().auth()
                .oauth2(mockToken.getTokenValue())
                .contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("isRatingPositive", equalTo(expectedIsRatingPositive));
    }
}
