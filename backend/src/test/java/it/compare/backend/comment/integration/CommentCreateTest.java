package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import({ProductTestDataFactory.class})
class CommentCreateTest extends CommentTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

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

    @Test
    void shouldReturnValidationErrorIfCommentIsBlank() {
        var product = productTestDataFactory.createOne();
        var body = new CommentDto("");

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
    void shouldReturnValidationErrorIfCommentLengthIsLessThan10() {
        var product = productTestDataFactory.createOne();
        var body = new CommentDto("text");

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
    void shouldReturnValidationErrorIfCommentLengthIsMoreThan2000() {
        var product = productTestDataFactory.createOne();
        var body = new CommentDto("a".repeat(2001));

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
