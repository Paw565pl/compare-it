package it.compare.backend.comment.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

@Import({ProductTestDataFactory.class, CommentTestDataFactory.class})
class CommentListTest extends CommentTest {

    @Autowired
    private ProductTestDataFactory productTestDataFactory;

    @Autowired
    private CommentTestDataFactory commentTestDataFactory;

    private Product testProduct;
    private List<Comment> testComments;
    private static final int NUMBER_OF_COMMENTS = 3;

    @BeforeEach
    void setupListTest() {
        testProduct = productTestDataFactory.createOne();
        testComments = commentTestDataFactory.createMultipleCommentsForProduct(testProduct, user, NUMBER_OF_COMMENTS);
    }

    @Test
    void shouldReturnNotFoundForNonExistentProduct() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        assertThat(commentRepository.count(), equalTo(3L));
    }

    @Test
    void shouldReturnNotFoundForNonExistentComment() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", testProduct.getId(), new ObjectId().toString())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        assertThat(commentRepository.count(), equalTo(3L));
    }

    @Test
    void shouldReturnNotFoundForCommentFromDifferentProduct() {
        var anotherProduct = productTestDataFactory.createOne();
        var comment = testComments.getFirst();

        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", anotherProduct.getId(), comment.getId())
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
        assertThat(commentRepository.count(), equalTo(3L));
    }

    @Test
    void shouldReturnCommentById() {
        var comment = testComments.getFirst();

        given().contentType(JSON)
                .when()
                .get("/{productId}/comments/{commentId}", testProduct.getId(), comment.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(comment.getId()))
                .body("text", equalTo(comment.getText()))
                .body("author", equalTo(user.getUsername()));
        assertThat(commentRepository.count(), equalTo(3L));
    }

    @Test
    void shouldReturnAllCommentsForProduct() {
        given().contentType(JSON)
                .when()
                .get("/{productId}/comments", testProduct.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(3))
                .body("content.findAll { it.author == '" + user.getUsername() + "' }", hasSize(NUMBER_OF_COMMENTS));
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
        assertThat(commentRepository.count(), equalTo(3L));
    }
}
