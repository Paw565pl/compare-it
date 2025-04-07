package it.compare.backend.rating.integration;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.model.Comment;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import it.compare.backend.rating.datafactory.RatingTestDataFactory;
import it.compare.backend.rating.repository.RatingRepository;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({
    UserTestDataFactory.class,
    CommentTestDataFactory.class,
    ProductTestDataFactory.class,
    RatingTestDataFactory.class
})
abstract class RatingTest extends IntegrationTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    UserTestDataFactory userTestDataFactory;

    @Autowired
    CommentTestDataFactory commentTestDataFactory;

    @Autowired
    ProductTestDataFactory productTestDataFactory;

    @Autowired
    RatingTestDataFactory ratingTestDataFactory;

    @Autowired
    RatingRepository ratingRepository;

    User user;
    Jwt mockToken;
    Product testProduct;
    Comment testComment;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/products");

        user = userTestDataFactory.createOne();
        mockToken = AuthMock.getToken(user.getId(), user.getUsername(), user.getEmail(), List.of());
        testProduct = productTestDataFactory.createOne();
        testComment = commentTestDataFactory.createCommentForProduct(testProduct);
        testComment.setAuthor(user);
        testComment = commentTestDataFactory.commentRepository.save(testComment);
    }

    @AfterEach
    void tearDown() {
        ratingTestDataFactory.clear();
    }
}
