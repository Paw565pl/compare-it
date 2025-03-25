package it.compare.backend.product.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;

import it.compare.backend.product.model.Category;
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

    @Test
    void shouldReturnFilteredProductsByCategory(){
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

}
