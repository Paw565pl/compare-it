package it.compare.backend.comment.integration;

import it.compare.backend.auth.model.User;
import it.compare.backend.comment.datafactory.CommentTestDataFactory;
import it.compare.backend.comment.repository.CommentRepository;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.core.test.IntegrationTest;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({UserTestDataFactory.class, CommentTestDataFactory.class})
abstract class CommentTest extends IntegrationTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    UserTestDataFactory userTestDataFactory;

    @Autowired
    CommentTestDataFactory commentTestDataFactory;

    @Autowired
    CommentRepository commentRepository;

    User user;
    Jwt mockToken;

    @BeforeEach
    void setUp() {
        setBaseUrl("/api/v1/products");

        user = userTestDataFactory.createOne();
        mockToken = AuthMock.getToken(user.getId(), user.getUsername(), user.getEmail(), List.of());
    }

    @AfterEach
    void tearDown() {
        commentTestDataFactory.clear();
    }
}
