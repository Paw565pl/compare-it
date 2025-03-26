package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ProductListTest extends ProductTest {

    @Test
    void shouldReturnAllProducts() {
        var productsCount = productTestDataFactory.createMany(3).size();
        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(productsCount));
    }

    static Stream<Arguments> categoryTestCases() {
        return Stream.of(
                Arguments.of(Category.PROCESSORS.getHumanReadableName(), 3),
                Arguments.of(Category.GRAPHICS_CARDS.getHumanReadableName(), 2),
                Arguments.of(Category.MOTHERBOARDS.getHumanReadableName(), 1),
                Arguments.of(Category.RAM_MEMORY.getHumanReadableName(), 0));
    }

    @ParameterizedTest
    @MethodSource("categoryTestCases")
    void shouldReturnFilteredProductsByCategory(String categoryName, int expectedCount) {
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.GRAPHICS_CARDS);
        productTestDataFactory.createProductWithCategory(Category.GRAPHICS_CARDS);
        productTestDataFactory.createProductWithCategory(Category.MOTHERBOARDS);

        given().contentType(JSON)
                .param("category", categoryName)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(expectedCount));
    }

    @ParameterizedTest
    @CsvSource({"1000, 5000, 5", "1000, 3000, 3", "3000, 4000, 2", "4000, 5000, 2", "5000, 6000, 1", "6000, 7000, 0"})
    void shouldReturnFilteredProductsByPrice(int minPrice, int maxPrice, int expectedCount) {
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(1000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(2000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(3000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(4000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(5000), "PLN", Condition.NEW);

        given().contentType(JSON)
                .param("minPrice", minPrice)
                .param("maxPrice", maxPrice)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(expectedCount));
    }

    static Stream<Arguments> shopTestCases() {
        return Stream.of(
                Arguments.of(Shop.RTV_EURO_AGD.getHumanReadableName(), 2),
                Arguments.of(Shop.MEDIA_EXPERT.getHumanReadableName(), 3),
                Arguments.of(Shop.MORELE_NET.getHumanReadableName(), 0));
    }

    @ParameterizedTest
    @MethodSource("shopTestCases")
    void shouldReturnFilteredProductsByShop(String shopName, int expectedCount) {
        productTestDataFactory.createProductWithShop(Shop.RTV_EURO_AGD);
        productTestDataFactory.createProductWithShop(Shop.RTV_EURO_AGD);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);

        given().contentType(JSON)
                .param("shop", shopName)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(expectedCount));
    }

    @ParameterizedTest
    @CsvSource({"'Intel i9 10900k', 1", "'Intel i9', 2", "'Intel i', 4", "AMD, 3", "Ryzen, 2"})
    void shouldReturnFilteredProductsByName(String name, int expectedCount) {
        productTestDataFactory.createProductWithName("Intel i9 10900k");
        productTestDataFactory.createProductWithName("Intel i8 10900k");
        productTestDataFactory.createProductWithName("Intel i5 10900k");
        productTestDataFactory.createProductWithName("Intel i9 10400k");
        productTestDataFactory.createProductWithName("AMD FX-6300");
        productTestDataFactory.createProductWithName("AMD RYZEN 5 3600");
        productTestDataFactory.createProductWithName("AMD RYZEN 7 3700x");

        given().contentType(JSON)
                .param("name", name)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(expectedCount));
    }

    @Test
    void shouldReturnSortedProductsByLowestCurrentPrice() {
        // One offer
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(1000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(2000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(3000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(4000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(5000), "PLN", Condition.NEW);

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].lowestCurrentPrice", equalTo(1000))
                .body("content[1].lowestCurrentPrice", equalTo(2000))
                .body("content[2].lowestCurrentPrice", equalTo(3000))
                .body("content[3].lowestCurrentPrice", equalTo(4000))
                .body("content[4].lowestCurrentPrice", equalTo(5000));

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,desc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].lowestCurrentPrice", equalTo(5000))
                .body("content[1].lowestCurrentPrice", equalTo(4000))
                .body("content[2].lowestCurrentPrice", equalTo(3000))
                .body("content[3].lowestCurrentPrice", equalTo(2000))
                .body("content[4].lowestCurrentPrice", equalTo(1000));

        productTestDataFactory.clear();

        // Multiple offers
        var today = LocalDateTime.now();
        var todayEarly = LocalDateTime.now().minusHours(1);
        var yesterday = LocalDateTime.now().minusDays(1);
        var fiveDaysAgo = LocalDateTime.now().minusDays(5);

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(1000),
                today,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(2000),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(3000),
                todayEarly);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(4000),
                today,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(5000),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(1000),
                fiveDaysAgo);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(5000),
                today,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(6000),
                todayEarly,
                Shop.MORELE_NET,
                BigDecimal.valueOf(2000),
                yesterday);

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].lowestCurrentPrice", equalTo(1000))
                .body("content[1].lowestCurrentPrice", equalTo(2000))
                .body("content[2].lowestCurrentPrice", equalTo(4000));

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,desc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].lowestCurrentPrice", equalTo(4000))
                .body("content[1].lowestCurrentPrice", equalTo(2000))
                .body("content[2].lowestCurrentPrice", equalTo(1000));
    }

    @Test
    void shouldReturnSortedProductsByOffersCount() {

        var now = LocalDateTime.now();
        var yesterday = LocalDateTime.now().minusDays(1);
        var fiveDaysAgo = LocalDateTime.now().minusDays(5);

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(110),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                yesterday);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                yesterday);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                yesterday,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo);
        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                now,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                now);

        given().contentType(JSON)
                .param("sort", "offersCount,asc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].offersCount", equalTo(0))
                .body("content[1].offersCount", equalTo(1))
                .body("content[2].offersCount", equalTo(2))
                .body("content[3].offersCount", equalTo(3))
                .body("content[4].offersCount", equalTo(3));

        given().contentType(JSON)
                .param("sort", "offersCount,desc")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("content[0].offersCount", equalTo(3))
                .body("content[1].offersCount", equalTo(3))
                .body("content[2].offersCount", equalTo(2))
                .body("content[3].offersCount", equalTo(1))
                .body("content[4].offersCount", equalTo(0));
    }

    @Test
    void shouldReturnProductsWithCorrectLowestCurrentPriceAndOffersCount() {
        var now = LocalDateTime.now();
        var twoDaysAgo = LocalDateTime.now().minusDays(2);
        var todayEarly = LocalDateTime.now().minusHours(1);
        var yesterday = LocalDateTime.now().minusDays(1);
        var fiveDaysAgo = LocalDateTime.now().minusDays(5);

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                todayEarly,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(110),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(110),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo);

        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "content[0].lowestCurrentPrice",
                        equalTo(110),
                        "content[0].lowestPriceShop",
                        equalTo(Shop.RTV_EURO_AGD.getHumanReadableName()),
                        "content[0].lowestPriceCurrency",
                        equalTo("PLN"),
                        "content[0].offersCount",
                        equalTo(2));

        productTestDataFactory.clear();

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                todayEarly,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(95),
                now,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(150),
                yesterday,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                twoDaysAgo);

        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "content[0].lowestCurrentPrice",
                        equalTo(90),
                        "content[0].lowestPriceShop",
                        equalTo(Shop.MORELE_NET.getHumanReadableName()),
                        "content[0].lowestPriceCurrency",
                        equalTo("PLN"),
                        "content[0].offersCount",
                        equalTo(3));

        productTestDataFactory.clear();

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                twoDaysAgo);

        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "content[0].lowestCurrentPrice",
                        equalTo(90),
                        "content[0].lowestPriceShop",
                        equalTo(Shop.MORELE_NET.getHumanReadableName()),
                        "content[0].lowestPriceCurrency",
                        equalTo("PLN"),
                        "content[0].offersCount",
                        equalTo(1));

        productTestDataFactory.clear();

        productTestDataFactory.createProductWithShopsPricesAndTimes(
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(100),
                fiveDaysAgo,
                Shop.RTV_EURO_AGD,
                BigDecimal.valueOf(110),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(70),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(150),
                fiveDaysAgo,
                Shop.MEDIA_EXPERT,
                BigDecimal.valueOf(110),
                fiveDaysAgo,
                Shop.MORELE_NET,
                BigDecimal.valueOf(90),
                fiveDaysAgo);

        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body(
                        "content[0].lowestCurrentPrice",
                        equalTo(null),
                        "content[0].lowestPriceShop",
                        equalTo(null),
                        "content[0].lowestPriceCurrency",
                        equalTo(null),
                        "content[0].offersCount",
                        equalTo(0));
    }
}
