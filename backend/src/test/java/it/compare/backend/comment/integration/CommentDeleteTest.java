package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.auth.model.Role;
import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import({ProductTestDataFactory.class, UserTestDataFactory.class, CommentTestDataFactory.class})
class CommentDeleteTest extends CommentTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    private Product testProduct;
    private Comment testComment;

    @BeforeEach
    void setUpDeleteTest() {
        testProduct = productTestDataFactory.createOne();
        testComment = commentTestDataFactory.createCommentForProduct(testProduct);
        testComment.setAuthor(user);
        testComment = commentRepository.save(testComment);
    }

    @Test
    void shouldReturnUnauthorized() {
        given().contentType(JSON)
                .when()
                .delete("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        assertThat(commentRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfProductDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}", new ObjectId().toString(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

        assertThat(commentRepository.count(), equalTo(1L));
    }

    @Test
    void shouldReturnNotFoundIfCommentDoesNotExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}", testProduct.getId(), new ObjectId().toString())
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

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}", testProduct.getId(), commentWithDifferentAuthor.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        assertThat(commentRepository.count(), equalTo(2L));
    }

    @Test
    void shouldReturnNoContentAfterDeletingComment() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(commentRepository.count(), equalTo(0L));
    }

    @Test
    void shouldReturnNoContentAfterDeletingCommentByAdmin() {
        var adminUser = userTestDataFactory.createOne();
        var mockAdminToken = AuthMock.getToken(
                adminUser.getId(), adminUser.getUsername(), adminUser.getEmail(), List.of(Role.ADMIN));
        when(jwtDecoder.decode(anyString())).thenReturn(mockAdminToken);

        given().contentType(JSON)
                .auth()
                .oauth2(mockAdminToken.getTokenValue())
                .when()
                .delete("/{productId}/comments/{commentId}", testProduct.getId(), testComment.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(commentRepository.count(), equalTo(0L));
    }
}
