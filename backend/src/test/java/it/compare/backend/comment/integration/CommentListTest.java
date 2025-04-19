package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import it.compare.backend.comment.model.Comment;
import it.compare.backend.product.model.Product;
import it.compare.backend.rating.datafactory.RatingTestDataFactory;
import java.util.List;
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
class CommentListTest extends CommentTest {

    @Autowired
    private RatingTestDataFactory ratingTestDataFactory;

    private Product testProduct;
    private List<Comment> testComments;
    private static final int NUMBER_OF_COMMENTS = 3;

    @BeforeEach
    void setup() {
        testProduct = productTestDataFactory.createOne();
        testComments = commentTestDataFactory.createMultipleCommentsForProduct(testProduct, user, NUMBER_OF_COMMENTS);
    }

    @Override
    @AfterEach
    void tearDown() {
        super.tearDown();
        ratingTestDataFactory.clear();
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnAllCommentsForProduct() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(3))
                .body(
                        String.format("content.findAll { it.author == \"%s\" }", user.getUsername()),
                        hasSize(NUMBER_OF_COMMENTS));
        assertThat(commentRepository.count(), equalTo(3L));
    }

    @Test
    void shouldReturnPaginatedResults() {
        given().contentType(JSON)
                .queryParam("page", 0)
                .queryParam("size", 2)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(2))
                .body("page.totalElements", equalTo(NUMBER_OF_COMMENTS))
                .body("page.totalPages", equalTo(2));
    }

    @Test
    void shouldAlwaysReturnIsRatingPositiveFieldAsNullIfUserIsNotAuthenticated() {
        var testComment = testComments.getFirst();
        ratingTestDataFactory.createRatingForComment(testComment, user, true);

        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content.findAll { it.isRatingPositive == null }", hasSize(NUMBER_OF_COMMENTS));
    }

    static Stream<Arguments> commentIsRatingPositiveFieldTestCases() {
        return Stream.of(Arguments.of(null, null), Arguments.of(true, true), Arguments.of(false, false));
    }

    @ParameterizedTest
    @MethodSource("commentIsRatingPositiveFieldTestCases")
    void shouldReturnCorrectIsRatingPositiveFieldValue(Boolean ratingValue, Boolean expectedIsRatingPositive) {
        var testComment = testComments.getFirst();
        if (ratingValue != null) ratingTestDataFactory.createRatingForComment(testComment, user, ratingValue);

        given().auth()
                .oauth2(mockToken.getTokenValue())
                .contentType(JSON)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        String.format("content.find { it.id == '%s' }.isRatingPositive", testComment.getId()),
                        equalTo(expectedIsRatingPositive));
    }
}
