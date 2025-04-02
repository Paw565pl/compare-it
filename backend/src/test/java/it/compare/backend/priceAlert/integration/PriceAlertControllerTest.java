package it.compare.backend.priceAlert.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.pricealert.dto.PriceAlertDto;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    void shouldReturnAuthenticationError() {
        given().contentType(JSON).when().get().then().statusCode(401);
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
    void shouldReturnAllAlerts() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));
    }

    @Test
    void shouldReturnOnlyAlertsOfUser() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        priceAlertTestDataFactory.createOne();
        priceAlertTestDataFactory.createOne();

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));
    }

    @Test
    void shouldReturnPriceAlertsBasedOnActiveField() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert3 = priceAlertTestDataFactory.createInactiveAlert(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .queryParam("active", true)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .queryParam("active", false)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content.id", contains(alert3.getId()));
    }

    @Test
    void shouldReturnCode409AfterCreatingPriceAlertForTheSameProductWithActiveStatus() {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), BigDecimal.valueOf(100), true);
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(409);
    }

    @Test
    void shouldReturnCode201AfterCreatingPriceAlertForTheSameProductWithInactiveStatus() {
        var alert = priceAlertTestDataFactory.createInactiveAlert(testUser);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), BigDecimal.valueOf(100), true);
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(201);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content", hasSize(2));
    }

    @ParameterizedTest
    @CsvSource({"100, true", "200, false", "300, true", "500, false"})
    void shouldReturnCode201AfterCreatingPriceAlert(BigDecimal targetPrice, boolean outletAllowed) {
        var product = productTestDataFactory.createOne();
        var alertDto = new PriceAlertDto(product.getId(), targetPrice, outletAllowed);
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(201)
                .body("productId", equalTo(product.getId()))
                .body("targetPrice", equalTo(targetPrice.intValue()))
                .body("outletAllowed", equalTo(outletAllowed));
    }

    @Test
    void shouldReturnCode403AfterDeletePriceAlertThatDoesNotBelongToUser() {
        priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var anotherUser = userTestDataFactory.createOne();
        var anotherAlert = priceAlertTestDataFactory.createPriceAlertForUser(anotherUser);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .delete("/{alertId}", anotherAlert.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void shouldReturnCode204AfterDeletingPriceAlert() {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .when()
                .delete("/{alertId}", alert.getId())
                .then()
                .statusCode(204);
    }

    @Test
    void shouldReturnCode403AfterUpdatingPriceAlertThatDoesNotBelongToUser() {
        priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var anotherUser = userTestDataFactory.createOne();
        var anotherAlert = priceAlertTestDataFactory.createPriceAlertForUser(anotherUser);

        var alertDto = new PriceAlertDto(anotherAlert.getProduct().getId(), BigDecimal.valueOf(100), true);
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .body(alertDto)
                .when()
                .put("/{alertId}", anotherAlert.getId())
                .then()
                .statusCode(403);
    }

    @ParameterizedTest
    @CsvSource({"100, true", "200, false", "300, true", "500, false"})
    void shouldReturnUpdatedPriceAlert(BigDecimal targetPrice, boolean outletAllowed) {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), targetPrice, outletAllowed);
        given().contentType(JSON)
                .auth()
                .oauth2(BEARER_TOKEN)
                .body(alertDto)
                .when()
                .put("/{alertId}", alert.getId())
                .then()
                .statusCode(200)
                .body("productId", equalTo(alert.getProduct().getId()))
                .body("targetPrice", equalTo(targetPrice.intValue()))
                .body("outletAllowed", equalTo(outletAllowed));
    }
}
