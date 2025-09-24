package it.compare.backend.product.datafactory;

import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.product.model.*;
import it.compare.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.datafaker.Faker;
import org.bson.types.ObjectId;
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
        var priceStamp = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), Currency.PLN, Condition.NEW);
        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url(), priceStamp);

        var product = new Product(
                String.valueOf(faker.number().positive()), faker.commerce().productName(), Category.CPU);

        product.getOffers().add(offer);
        product.setId(new ObjectId().toString());
        product.setComputedState(ComputedState.fromProduct(product));

        return product;
    }

    @Override
    public Product createOne() {
        return productRepository.save(generate());
    }

    @Override
    public List<Product> createMany(int count) {
        var products = new ArrayList<Product>();
        for (int i = 0; i < count; i++) {
            products.add(generate());
        }

        return productRepository.saveAll(products);
    }

    @Override
    public void clear() {
        productRepository.deleteAll();
    }

    public void createProductWithCategory(Category category) {
        var product = generate();

        product.setCategory(category);
        product.setComputedState(ComputedState.fromProduct(product));

        productRepository.save(product);
    }

    public void createProductWithPriceStamp(BigDecimal price, Currency currency, Condition condition) {
        var product = generate();

        var customPriceStamp = new PriceStamp(price, currency, condition);
        customPriceStamp.setTimestamp(Instant.now());
        product.getOffers().clear();

        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url(), customPriceStamp);
        product.getOffers().add(offer);

        product.setComputedState(ComputedState.fromProduct(product));
        productRepository.save(product);
    }

    public void createProductWithShop(Shop shop) {
        var priceStamp = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), Currency.PLN, Condition.NEW);
        var offer = new Offer(shop, faker.internet().url(), priceStamp);

        var product = generate();
        product.getOffers().clear();
        product.getOffers().add(offer);
        product.setComputedState(ComputedState.fromProduct(product));

        productRepository.save(product);
    }

    public void createProductWithName(String name) {
        var product = generate();

        product.setName(name);
        product.setComputedState(ComputedState.fromProduct(product));

        productRepository.save(product);
    }

    public record OfferPriceStamp(Shop shop, BigDecimal price, Instant timestamp) {}

    public Product createProductWithOffers(List<OfferPriceStamp> offerPriceStamps) {
        var product = generate();
        product.getOffers().clear();

        offerPriceStamps.forEach(offerPriceStamp -> {
            var priceStamp = new PriceStamp(offerPriceStamp.price(), Currency.PLN, Condition.NEW);
            priceStamp.setTimestamp(offerPriceStamp.timestamp());

            var offer = product.getOffers().stream()
                    .filter(o -> o.getShop().equals(offerPriceStamp.shop))
                    .findFirst();

            if (offer.isPresent()) {
                offer.get().addPriceStamp(priceStamp);
            } else {
                var newOffer =
                        new Offer(offerPriceStamp.shop(), faker.internet().url(), priceStamp);
                newOffer.addPriceStamp(priceStamp);
                product.getOffers().add(newOffer);
            }
        });

        product.setComputedState(ComputedState.fromProduct(product));
        return productRepository.save(product);
    }
}
