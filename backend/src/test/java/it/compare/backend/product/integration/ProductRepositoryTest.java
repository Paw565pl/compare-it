package it.compare.backend.product.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.util.Collections;
import java.util.List;
import net.datafaker.Faker;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProductRepositoryTest extends ProductTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Faker faker;

    @Test
    void shouldReturnEmptyListWhenEanListIsEmpty() {
        List<String> emptyEanList = Collections.emptyList();

        var products = productRepository.findAllByEanIn(emptyEanList);

        assertThat(products, is(empty()));
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsMatchEans() {
        var nonExistentEans = List.of(
                String.valueOf(faker.number().positive()),
                String.valueOf(faker.number().positive()));

        var products = productRepository.findAllByEanIn(nonExistentEans);

        assertThat(products, is(empty()));
    }

    @Test
    void shouldReturnListOfOnlyMatchingProducts() {
        var product1 = productTestDataFactory.createOne();
        var product2 = productTestDataFactory.createOne();
        var product3 = productTestDataFactory.createOne();

        var partialEanList = List.of(product1.getEan(), product3.getEan());
        var products = productRepository.findAllByEanIn(partialEanList);

        assertThat(products.size(), is(2));
        assertThat(
                products.stream().map(Product::getEan).toList(),
                containsInAnyOrder(product1.getEan(), product3.getEan()));
        assertThat(products.stream().map(Product::getEan).toList(), not(contains(product2.getEan())));
    }

    @Test
    void shouldReturnListProductByEanList() {
        var product = productTestDataFactory.createOne();
        var product2 = productTestDataFactory.createOne();
        var product3 = productTestDataFactory.createOne();

        var eanList = List.of(product.getEan(), product2.getEan(), product3.getEan());

        var products = productRepository.findAllByEanIn(eanList);

        assertThat(products.size(), Matchers.is(3));
        assertThat(
                products.stream().map(Product::getEan).toList(),
                containsInAnyOrder(product.getEan(), product2.getEan(), product3.getEan()));
    }

    @Test
    void shouldReturnListProductWithoutDuplicates() {
        var product = productTestDataFactory.createOne();

        var duplicateEanList = List.of(product.getEan(), product.getEan(), product.getEan());
        var products = productRepository.findAllByEanIn(duplicateEanList);

        assertThat(products.size(), is(1));
        assertThat(products.getFirst().getEan(), equalTo(product.getEan()));
    }
}
