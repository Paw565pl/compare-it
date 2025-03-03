package it.compare.backend.product.datafactory;

import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.product.model.*;
import it.compare.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import net.datafaker.Faker;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import(FakerConfig.class)
public class ProductTestDataFactory implements TestDataFactory<Product> {

    private final Faker faker;
    private final ProductRepository productRepository;

    public ProductTestDataFactory(Faker faker, ProductRepository productRepository) {
        this.faker = faker;
        this.productRepository = productRepository;
    }

    @Override
    public Product generate() {
        var priceStamp = new PriceStamp(
                BigDecimal.valueOf(faker.number().positive()),
                faker.currency().toString(),
                faker.bool().bool(),
                Condition.NEW);
        var offer = new Offer(
                Shop.RTV_EURO_AGD, faker.internet().url(), faker.internet().url());
        offer.getPriceHistory().add(priceStamp);

        var product = new Product(
                String.valueOf(faker.number().positive()), faker.commerce().productName(), Category.PROCESSORS);
        product.getOffers().add(offer);

        return product;
    }

    @Override
    public Product createOne() {
        return productRepository.save(generate());
    }

    @Override
    public Collection<Product> createMany(int count) {
        var products = new ArrayList<Product>();
        for (int i = 0; i < count; i++) {
            products.add(createOne());
        }

        return products;
    }

    @Override
    public void clear() {
        productRepository.deleteAll();
    }
}
