package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ProductDetailsTest extends ProductTest {

    @Test
    void shouldReturn404WhenProductNotFound() {
        given().contentType(JSON).when().get("/67e2ca0874361032fa28d805").then().statusCode(404);
    }

    @Test
    void shouldReturn400WhenProductIdIsInvalid() {
        given().contentType(JSON).when().get("/invalid").then().statusCode(400);
    }

    @Test
    void shouldReturnProductDetails() {
        var product = productTestDataFactory.createOne();

        given().contentType(JSON)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(product.getId()))
                .body("name", equalTo(product.getName()))
                .body("category", equalTo(product.getCategory().getHumanReadableName()))
                .body("offers", hasSize(1));

        var now = LocalDateTime.now();
        var twoDaysAgo = LocalDateTime.now().minusDays(2);
        var fiveDaysAgo = LocalDateTime.now().minusDays(5);

        var product2 = productTestDataFactory.createProductWithMultipleOffers(
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                twoDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                twoDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(200),
                fiveDaysAgo);

        given().contentType(JSON)
                .when()
                .get("/{productId}", product2.getId())
                .then()
                .statusCode(200)
                .body("offers.size()", equalTo(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.isAvailable",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        equalTo(true))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.isAvailable",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        equalTo(true))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.isAvailable", Shop.MORELE_NET.getHumanReadableName()),
                        equalTo(false));
    }

    @Test
    void shouldReturnOffersFilteredByPriceStampRangeDays() {
        var now = LocalDateTime.now();
        var twoDaysAgo = LocalDateTime.now().minusDays(2);
        var fiveDaysAgo = LocalDateTime.now().minusDays(5);
        var yearAgo = LocalDateTime.now().minusYears(1);
        var weekAgo = LocalDateTime.now().minusWeeks(1);
        var eightyNineDaysAgo = LocalDateTime.now().minusDays(89);
        var ninetyOneDaysAgo = LocalDateTime.now().minusDays(91);
        var product = productTestDataFactory.createProductWithMultipleOffers(
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                twoDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                weekAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(100),
                eightyNineDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                twoDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                weekAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                fiveDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(200),
                ninetyOneDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(200),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(200),
                weekAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(100),
                yearAgo);

        given().contentType(JSON)
                .param("priceStampRangeDays", 3)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("offers", hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        hasSize(2))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        hasSize(1))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.getHumanReadableName()),
                        hasSize(0));

        given().contentType(JSON)
                .param("priceStampRangeDays", 10)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("offers", hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.getHumanReadableName()),
                        hasSize(2));

        // check if the priceStampRangeDays has a minimum value of 1
        given().contentType(JSON)
                .param("priceStampRangeDays", -1)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("offers", hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        hasSize(1))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        hasSize(0))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.getHumanReadableName()),
                        hasSize(0));

        // check if the priceStampRangeDays has a maximum value of 180
        given().contentType(JSON)
                .param("priceStampRangeDays", 400)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("offers", hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        hasSize(4))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        hasSize(4))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.getHumanReadableName()),
                        hasSize(2));

        // check if the priceStampRangeDays has a default value of 90
        given().contentType(JSON)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(200)
                .body("offers", hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.MEDIA_EXPERT.getHumanReadableName()),
                        hasSize(4))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory",
                                Shop.RTV_EURO_AGD.getHumanReadableName()),
                        hasSize(3))
                .body(
                        String.format(
                                "offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.getHumanReadableName()),
                        hasSize(2));
    }
}
