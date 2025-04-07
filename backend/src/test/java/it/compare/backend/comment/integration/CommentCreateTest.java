package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import({ProductTestDataFactory.class})
class CommentCreateTest extends CommentTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    @Test
    void shouldReturnUnauthorized() {
        var body = new CommentDto("test comment");

        given().contentType(JSON)
                .body(body)
                .when()
                .post("/{productId}/comments", new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(commentRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var body = new CommentDto("test comment");

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments", new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(commentRepository.count(), equalTo(0L));
    }

    static Stream<String> invalidCommentTextProvider() {
        return Stream.of("", "yes", "text", "a".repeat(2001), "a".repeat(2000) + "b");
    }

    @ParameterizedTest()
    @MethodSource("invalidCommentTextProvider")
    void shouldReturnValidationErrorForInvalidCommentLength(String commentText) {
        var product = productTestDataFactory.createOne();
        var body = new CommentDto(commentText);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments", product.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        assertThat(commentRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnCreatedAfterCreatingComment() {
        var product = productTestDataFactory.createOne();
        var commentText = "test comment";
        var body = new CommentDto(commentText);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(body)
                .when()
                .post("/{productId}/comments", product.getId())
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("author", equalTo(user.getUsername()))
                .body("text", equalTo(commentText));

        assertThat(commentRepository.count(), equalTo(1L));
    }
}
