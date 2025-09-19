package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Currency;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

class ProductListTest extends ProductTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 25, 125, 320, 1000})
    void shouldReturnAllProducts(int numberOfProducts) {
        var productsCount = productTestDataFactory.createMany(numberOfProducts).size();
        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(productsCount))
                .body("page.size", equalTo(20))
                .body("page.totalPages", equalTo((int) Math.ceil(productsCount / 20.0)));
    }

    static Stream<Arguments> categoryTestCases() {
        return Stream.of(
                Arguments.of(Category.CPU.name(), 3),
                Arguments.of(Category.GPU.name(), 2),
                Arguments.of(Category.MOTHERBOARD.name(), 1),
                Arguments.of(Category.RAM_MEMORY.name(), 0));
    }

    @ParameterizedTest
    @MethodSource("categoryTestCases")
    void shouldReturnFilteredProductsByCategory(String categoryName, int expectedCount) {
        productTestDataFactory.createProductWithCategory(Category.CPU);
        productTestDataFactory.createProductWithCategory(Category.CPU);
        productTestDataFactory.createProductWithCategory(Category.CPU);
        productTestDataFactory.createProductWithCategory(Category.GPU);
        productTestDataFactory.createProductWithCategory(Category.GPU);
        productTestDataFactory.createProductWithCategory(Category.MOTHERBOARD);

        given().contentType(JSON)
                .param("category", categoryName)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(expectedCount));
    }

    @ParameterizedTest
    @CsvSource({"1000, 5000, 5", "1000, 3000, 3", "3000, 4000, 2", "4000, 5000, 2", "5000, 6000, 1", "6000, 7000, 0"})
    void shouldReturnFilteredProductsByPrice(int minPrice, int maxPrice, int expectedCount) {
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(1000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(2000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(3000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(4000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(5000), Currency.PLN, Condition.NEW);

        given().contentType(JSON)
                .param("minPrice", minPrice)
                .param("maxPrice", maxPrice)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
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
                .statusCode(HttpStatus.OK.value())
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
                .statusCode(HttpStatus.OK.value())
                .body("page.totalElements", equalTo(expectedCount));
    }

    @Test
    void shouldReturnSortedProductsByLowestCurrentPrice() {
        // One offer
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(1000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(2000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(3000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(4000), Currency.PLN, Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(5000), Currency.PLN, Condition.NEW);

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,asc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
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
                .statusCode(HttpStatus.OK.value())
                .body("content[0].lowestCurrentPrice", equalTo(5000))
                .body("content[1].lowestCurrentPrice", equalTo(4000))
                .body("content[2].lowestCurrentPrice", equalTo(3000))
                .body("content[3].lowestCurrentPrice", equalTo(2000))
                .body("content[4].lowestCurrentPrice", equalTo(1000));

        productTestDataFactory.clear();

        var today = Instant.now();
        var todayEarly = Instant.now().minus(Duration.ofHours(1));
        var yesterday = Instant.now().minus(Duration.ofDays(1));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));

        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(1000), today),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(2000), yesterday),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(3000), todayEarly)));

        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(4000), today),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(5000), yesterday),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(1000), fiveDaysAgo)));
        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(5000), today),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(6000), todayEarly),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(2000), yesterday)));

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,asc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content[0].lowestCurrentPrice", equalTo(1000))
                .body("content[1].lowestCurrentPrice", equalTo(2000))
                .body("content[2].lowestCurrentPrice", equalTo(4000));

        given().contentType(JSON)
                .param("sort", "lowestCurrentPrice,desc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content[0].lowestCurrentPrice", equalTo(4000))
                .body("content[1].lowestCurrentPrice", equalTo(2000))
                .body("content[2].lowestCurrentPrice", equalTo(1000));
    }

    @Test
    void shouldReturnSortedProductsByOffersCount() {
        var now = Instant.now();
        var yesterday = Instant.now().minus(Duration.ofDays(1));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));

        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), now),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(110), yesterday),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), yesterday)));
        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), now),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), yesterday),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), yesterday)));
        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), yesterday),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo)));
        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo)));
        productTestDataFactory.createProductWithOffers(List.of(
                new ProductTestDataFactory.OfferPriceStamp(Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), now),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo),
                new ProductTestDataFactory.OfferPriceStamp(Shop.MORELE_NET, BigDecimal.valueOf(90), now)));

        given().contentType(JSON)
                .param("sort", "offersCount,asc")
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
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
                .statusCode(HttpStatus.OK.value())
                .body("content[0].offersCount", equalTo(3))
                .body("content[1].offersCount", equalTo(3))
                .body("content[2].offersCount", equalTo(2))
                .body("content[3].offersCount", equalTo(1))
                .body("content[4].offersCount", equalTo(0));
    }

    static Stream<Arguments> productLowestCurrentPriceAndOffersCountTestCases() {
        var now = Instant.now();
        var twoDaysAgo = Instant.now().minus(Duration.ofDays(2));
        var todayEarly = Instant.now().minus(Duration.ofHours(1));
        var yesterday = Instant.now().minus(Duration.ofDays(1));
        var fiveDaysAgo = Instant.now().minus(Duration.ofDays(5));

        return Stream.of(
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), todayEarly),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(110), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(110), yesterday),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo)),
                        110,
                        Shop.RTV_EURO_AGD.getHumanReadableName(),
                        Currency.PLN,
                        2),
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), todayEarly),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(95), now),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(150), yesterday),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(90), twoDaysAgo)),
                        90,
                        Shop.MORELE_NET.getHumanReadableName(),
                        Currency.PLN,
                        3),
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(90), twoDaysAgo)),
                        90,
                        Shop.MORELE_NET.getHumanReadableName(),
                        Currency.PLN,
                        1),
                Arguments.of(
                        List.of(
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(100), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.RTV_EURO_AGD, BigDecimal.valueOf(110), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(70), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(150), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MEDIA_EXPERT, BigDecimal.valueOf(110), fiveDaysAgo),
                                new ProductTestDataFactory.OfferPriceStamp(
                                        Shop.MORELE_NET, BigDecimal.valueOf(90), fiveDaysAgo)),
                        null,
                        null,
                        null,
                        0));
    }

    @ParameterizedTest
    @MethodSource("productLowestCurrentPriceAndOffersCountTestCases")
    void shouldReturnProductsWithCorrectLowestCurrentPriceAndOffersCount(
            List<ProductTestDataFactory.OfferPriceStamp> testData,
            Integer expectedLowestPrice,
            String expectedShop,
            String expectedCurrency,
            Integer expectedOffersCount) {

        productTestDataFactory.createProductWithOffers(testData);

        given().contentType(JSON)
                .when()
                .get()
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(
                        "content[0].lowestCurrentPrice",
                        equalTo(expectedLowestPrice),
                        "content[0].lowestPriceShop",
                        equalTo(expectedShop),
                        "content[0].lowestPriceCurrency",
                        equalTo(expectedCurrency),
                        "content[0].offersCount",
                        equalTo(expectedOffersCount));

        productTestDataFactory.clear();
    }
}
