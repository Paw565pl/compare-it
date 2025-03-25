package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Condition;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

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

    @Test
    void shouldReturnFilteredProductsByCategory() {
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.PROCESSORS);
        productTestDataFactory.createProductWithCategory(Category.GRAPHICS_CARDS);
        productTestDataFactory.createProductWithCategory(Category.GRAPHICS_CARDS);
        productTestDataFactory.createProductWithCategory(Category.MOTHERBOARDS);

        given().contentType(JSON)
                .param("category", Category.PROCESSORS.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(3));

        given().contentType(JSON)
                .param("category", Category.GRAPHICS_CARDS.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));

        given().contentType(JSON)
                .param("category", Category.MOTHERBOARDS.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(1));

        given().contentType(JSON)
                .param("category", Category.RAM_MEMORY.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnFilteredProductsByPrice() {
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(1000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(2000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(3000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(4000), "PLN", Condition.NEW);
        productTestDataFactory.createProductWithPriceStamp(BigDecimal.valueOf(5000), "PLN", Condition.NEW);

        given().contentType(JSON)
                .param("minPrice", 1000)
                .param("maxPrice", 5000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(5));

        given().contentType(JSON)
                .param("minPrice", 1000)
                .param("maxPrice", 3000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(3));

        given().contentType(JSON)
                .param("minPrice", 3000)
                .param("maxPrice", 4000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));

        given().contentType(JSON)
                .param("minPrice", 4000)
                .param("maxPrice", 5000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));

        given().contentType(JSON)
                .param("minPrice", 5000)
                .param("maxPrice", 6000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(1));

        given().contentType(JSON)
                .param("minPrice", 6000)
                .param("maxPrice", 7000)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnFilteredProductsByShop() {
        productTestDataFactory.createProductWithShop(Shop.RTV_EURO_AGD);
        productTestDataFactory.createProductWithShop(Shop.RTV_EURO_AGD);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);
        productTestDataFactory.createProductWithShop(Shop.MEDIA_EXPERT);

        given().contentType(JSON)
                .param("shop", Shop.RTV_EURO_AGD.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));

        given().contentType(JSON)
                .param("shop", Shop.MEDIA_EXPERT.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(3));

        given().contentType(JSON)
                .param("shop", Shop.MORELE_NET.getHumanReadableName())
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(0));
    }

    @Test
    void shouldReturnFilteredProductsByName() {
        productTestDataFactory.createProductWithName("Intel i9 10900k");
        productTestDataFactory.createProductWithName("Intel i8 10900k");
        productTestDataFactory.createProductWithName("Intel i5 10900k");
        productTestDataFactory.createProductWithName("Intel i9 10400k");
        productTestDataFactory.createProductWithName("AMD FX-6300");
        productTestDataFactory.createProductWithName("AMD RYZEN 5 3600");
        productTestDataFactory.createProductWithName("AMD RYZEN 7 3700x");

        given().contentType(JSON)
                .param("name", "Intel i9 10900k")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(1));

        given().contentType(JSON)
                .param("name", "Intel i9")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));

        given().contentType(JSON)
                .param("name", "Intel i")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(4));

        given().contentType(JSON)
                .param("name", "AMD")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(3));

        given().contentType(JSON)
                .param("name", "Ryzen")
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("page.totalElements", equalTo(2));
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
