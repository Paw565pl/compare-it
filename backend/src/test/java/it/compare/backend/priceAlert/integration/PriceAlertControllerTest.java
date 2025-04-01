package it.compare.backend.priceAlert.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.mock.AuthMock;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PriceAlertControllerTest extends PriceAlertTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private static final String BEARER_TOKEN = "mock-token";

    @BeforeEach
    void setup() {
        testUser = userTestDataFactory.createOne();
        var mockToken = AuthMock.getToken(testUser.getId(), Collections.emptyList());
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnEmptyListWhenNoAlertsExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .get()
                .then()
                .log()
                .ifValidationFails()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    @Test
    void shouldGetAllAlerts() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .get()
                .then()
                .log()
                .body()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));
    }
}
