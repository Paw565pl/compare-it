package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.dto.CommentDto;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import({ProductTestDataFactory.class, UserTestDataFactory.class, CommentTestDataFactory.class})
class CommentEditTest extends CommentTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    private Product testProduct;
    private Comment testComment;

    @BeforeEach
    void mockToken() {
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);

        testProduct = productTestDataFactory.createOne();

        testComment = commentTestDataFactory.createCommentForProduct(testProduct);
        testComment.setAuthor(user);
        testComment = commentRepository.save(testComment);
    }

    @Test
    void shouldReturnUnauthorized() {
        var commentDto = new CommentDto("Updated comment");

        given().contentType(JSON)
                .body(commentDto)
                .when()
                .put("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(commentRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        var commentDto = new CommentDto("Updated comment");

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(commentDto)
                .when()
                .put("/{productId}/comments/{commentId}", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(commentRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        var commentDto = new CommentDto("Updated comment");

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(commentDto)
                .when()
                .put("/{productId}/comments/{commentId}", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(commentRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnForbiddenIfUserIsNotCommentAuthor() {
        var anotherUser = userTestDataFactory.createOne();

        var commentWithDifferentAuthor = commentTestDataFactory.createCommentForProduct(testProduct);
        commentWithDifferentAuthor.setAuthor(anotherUser);
        commentWithDifferentAuthor = commentRepository.save(commentWithDifferentAuthor);

        var commentDto = new CommentDto("Updated comment");

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(commentDto)
                .when()
                .put("/{productId}/comments/{commentId}", testProduct.getId(), commentWithDifferentAuthor.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        assertThat(commentRepository.count(), equalTo(2L));
    }

    @Test
    void shouldSuccessfullyUpdateComment() {
        var updatedContent = "Updated comment content";
        var commentDto = new CommentDto(updatedContent);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(commentDto)
                .when()
                .put("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(testComment.getId()))
                .body("author", equalTo(user.getUsername()))
                .body("text", equalTo(updatedContent));

        assertThat(commentRepository.count(), equalTo(1L));
    }
}
