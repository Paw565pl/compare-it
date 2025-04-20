package it.compare.backend.pricealert.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.mock.AuthMock;
import it.compare.backend.pricealert.dto.PriceAlertDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class PriceAlertControllerTest extends PriceAlertTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private User testUser;
    private Jwt mockToken;

    @BeforeEach
    void setup() {
        testUser = userTestDataFactory.createOne();

        mockToken = AuthMock.getToken(testUser.getId(), testUser.getUsername(), testUser.getEmail(), List.of());
        when(jwtDecoder.decode(anyString())).thenReturn(mockToken);
    }

    @Test
    void shouldReturnAuthenticationError() {
        given().contentType(JSON).when().get().then().statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void shouldReturnEmptyListWhenNoAlertsExist() {
        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(0));
    }

    @Test
    void shouldReturnAllAlerts() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
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
                .oauth2(mockToken.getTokenValue())
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));
    }

    @Test
    void shouldReturnPriceAlertsBasedOnActiveField() {
        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alert3 = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(testUser, false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .queryParam("isActive", true)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(2))
                .body("content.id", containsInAnyOrder(alert1.getId(), alert2.getId()));

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .queryParam("isActive", false)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", hasSize(1))
                .body("content.id", contains(alert3.getId()));
    }

    @Test
    void shouldReturnConflictAfterCreatingPriceAlertForTheSameProductWithActiveStatus() {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), BigDecimal.valueOf(100), true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void shouldReturnCreatedAfterCreatingPriceAlertForTheSameProductWithInactiveStatus() {
        var alert = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(testUser, false);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), BigDecimal.valueOf(100), true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value());
        assertThat(priceAlertRepository.count(), is(2L));
    }

    @ParameterizedTest
    @CsvSource({"100, true", "200, false", "300, true", "500, false"})
    void shouldReturnCreatedAfterCreatingPriceAlert(BigDecimal targetPrice, boolean isOutletAllowed) {
        var product = productTestDataFactory.createOne();
        var alertDto = new PriceAlertDto(product.getId(), targetPrice, isOutletAllowed);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("productId", equalTo(product.getId()))
                .body("targetPrice", equalTo(targetPrice.intValue()))
                .body("isOutletAllowed", equalTo(isOutletAllowed));
        assertThat(priceAlertRepository.count(), is(1L));
    }

    @Test
    void shouldReturnForbiddenAfterDeletePriceAlertThatDoesNotBelongToUser() {
        priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var anotherUser = userTestDataFactory.createOne();
        var anotherAlert = priceAlertTestDataFactory.createPriceAlertForUser(anotherUser);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{alertId}", anotherAlert.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldReturnNoContentAfterDeletingPriceAlert() {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/{alertId}", alert.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        assertThat(priceAlertRepository.count(), is(0L));
    }

    @Test
    void shouldReturnNoContentAfterDeletingAllInactivePriceAlerts() {
        priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        priceAlertTestDataFactory.createPriceAlertWithActiveStatus(testUser, false);
        priceAlertTestDataFactory.createPriceAlertWithActiveStatus(testUser, false);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .when()
                .delete("/inactive")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
        assertThat(priceAlertRepository.count(), is(1L));
    }

    @Test
    void shouldReturnForbiddenAfterUpdatingPriceAlertThatDoesNotBelongToUser() {
        priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var anotherUser = userTestDataFactory.createOne();
        var anotherAlert = priceAlertTestDataFactory.createPriceAlertForUser(anotherUser);
        var alertDto = new PriceAlertDto(anotherAlert.getProduct().getId(), BigDecimal.valueOf(100), true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .put("/{alertId}", anotherAlert.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldReturnBadRequestAfterUpdatingPriceAlertThatIsInactive() {
        var alert = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(testUser, false);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), BigDecimal.valueOf(100), true);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .put("/{alertId}", alert.getId())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest
    @CsvSource({"100, true", "200, false", "300, true", "500, false"})
    void shouldReturnUpdatedPriceAlert(BigDecimal targetPrice, boolean outletAllowed) {
        var alert = priceAlertTestDataFactory.createPriceAlertForUser(testUser);
        var alertDto = new PriceAlertDto(alert.getProduct().getId(), targetPrice, outletAllowed);

        given().contentType(JSON)
                .auth()
                .oauth2(mockToken.getTokenValue())
                .body(alertDto)
                .when()
                .put("/{alertId}", alert.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("productId", equalTo(alert.getProduct().getId()))
                .body("targetPrice", equalTo(targetPrice.intValue()))
                .body("isOutletAllowed", equalTo(outletAllowed));
    }
}
