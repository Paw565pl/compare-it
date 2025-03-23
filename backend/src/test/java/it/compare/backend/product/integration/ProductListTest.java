package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class ProductListTest extends ProductTest {

    @Test
    void shouldReturnEmptyList() {
        given().contentType(JSON).when().get().then().statusCode(200).body("page.totalElements", equalTo(0));
    }

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
}
