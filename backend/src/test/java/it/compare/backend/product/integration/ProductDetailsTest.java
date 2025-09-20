package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class ProductDetailsTest extends ProductTest {

    @Test
    void shouldReturnNotFoundWhenProductNotFound() {
        var randomObjectId = new ObjectId().toString();

        given().contentType(JSON).when().get("/" + randomObjectId).then().statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnBadRequestWhenProductIdIsInvalid() {
        given().contentType(JSON).when().get("/invalid").then().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldReturnProductDetails() {
        var product = productTestDataFactory.createOne();

        given().contentType(JSON)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(product.getId()))
                .body("name", equalTo(product.getName()))
                .body("category", equalTo(product.getCategory().name()))
                .body("offers", hasSize(1));

        var now = Instant.now();
        var twoDaysAgo = Instant.now().minus(Duration.ofDays(2));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));

        var productWithMultipleOffers = productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), now),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), twoDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), twoDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(200), fiveDaysAgo)));

        given().contentType(JSON)
                .when()
                .get("/{productId}", productWithMultipleOffers.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("offers.size()", equalTo(3))
                .body(
                        String.format("offers.find { it.shop == '%s' }.isAvailable", Shop.MEDIA_EXPERT.name()),
                        equalTo(true))
                .body(
                        String.format("offers.find { it.shop == '%s' }.isAvailable", Shop.RTV_EURO_AGD.name()),
                        equalTo(true))
                .body(
                        String.format("offers.find { it.shop == '%s' }.isAvailable", Shop.MORELE_NET.name()),
                        equalTo(false));
    }

    static Stream<Arguments> priceStampRangeTestCases() {
        return Stream.of(
                Arguments.of(3, 2, 1, 0),
                Arguments.of(10, 3, 3, 2),
                Arguments.of(-1, 0, 0, 0), // check if the priceStampRangeDays has a minimum value of 0
                Arguments.of(400, 4, 4, 2), // check if the priceStampRangeDays has a maximum value of 180
                Arguments.of(Integer.MIN_VALUE, 3, 3, 2) // check if the priceStampRangeDays has a default value of 30
                );
    }

    @ParameterizedTest
    @MethodSource("priceStampRangeTestCases")
    void shouldReturnOffersFilteredByPriceStampRangeDays(
            int rangeDays, int mediaExpertHistorySize, int rtvEuroAgdHistorySize, int moreleNetHistorySize) {

        var now = Instant.now();
        var twoDaysAgo = Instant.now().minus(Duration.ofDays(2));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));
        var yearAgo = Instant.now().minus(Duration.ofDays(365));
        var weekAgo = Instant.now().minus(Duration.ofDays(7));
        var eightyNineDaysAgo = Instant.now().minus(Duration.ofDays(89));
        var ninetyOneDaysAgo = Instant.now().minus(Duration.ofDays(91));

        var product = productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), now),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), twoDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), weekAgo),
                new ProductTestDataFactory.OfferPriceStamp(
                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), eightyNineDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), twoDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), weekAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(
                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), ninetyOneDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(200), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(200), weekAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(110), yearAgo)));

        var request = given().contentType(JSON);

        // testing for default value
        if (rangeDays != Integer.MIN_VALUE) request = request.param("priceStampRangeDays", rangeDays);

        request.when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("offers", hasSize(3))
                .body(
                        String.format("offers.find { it.shop == '%s' }.priceHistory", Shop.MEDIA_EXPERT.name()),
                        hasSize(mediaExpertHistorySize))
                .body(
                        String.format("offers.find { it.shop == '%s' }.priceHistory", Shop.RTV_EURO_AGD.name()),
                        hasSize(rtvEuroAgdHistorySize))
                .body(
                        String.format("offers.find { it.shop == '%s' }.priceHistory", Shop.MORELE_NET.name()),
                        hasSize(moreleNetHistorySize));
    }

    static Stream<Arguments> sortOffersByLowestPriceTestCases() {
        var now = Instant.now();
        var twoDaysAgo = Instant.now().minus(Duration.ofDays(2));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));

        return Stream.of(
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(200), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(210), twoDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(400), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(300), twoDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(500), twoDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(100), now)),
                        List.of(Shop.MEDIA_EXPERT.name(), Shop.RTV_EURO_AGD.name(), Shop.MORELE_NET.name())),
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(199), twoDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(150), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(199), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(205), now)),
                        List.of(Shop.MEDIA_EXPERT.name(), Shop.RTV_EURO_AGD.name(), Shop.MORELE_NET.name())),
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(300), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(150), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(250), now)),
                        List.of(Shop.RTV_EURO_AGD.name(), Shop.MORELE_NET.name(), Shop.MEDIA_EXPERT.name())));
    }

    @ParameterizedTest
    @MethodSource("sortOffersByLowestPriceTestCases")
    void shouldReturnOffersSortedByLowestPriceOnLastPriceStamp(
            List<ProductTestDataFactory.OfferPriceStamp> offerPriceStamps, List<String> expectedShops) {
        var product = productTestDataFactory.createProductWithOffers(offerPriceStamps);

        given().contentType(JSON)
                .when()
                .get("/{productId}", product.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("offers[0].shop", equalTo(expectedShops.get(0)))
                .body("offers[1].shop", equalTo(expectedShops.get(1)))
                .body("offers[2].shop", equalTo(expectedShops.get(2)));
    }
}
