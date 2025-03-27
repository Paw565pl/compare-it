package it.compare.backend.product.datafactory;

import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.product.model.*;
import it.compare.backend.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
                BigDecimal.valueOf(faker.number().positive()), faker.currency().toString(), Condition.NEW);
        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
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

    public void createProductWithCategory(Category category) {
        var product = generate();
        product.setCategory(category);
        productRepository.save(product);
    }

    public void createProductWithPriceStamp(BigDecimal price, String currency, Condition condition) {
        var product = generate();

        var customPriceStamp = new PriceStamp(price, currency, condition);
        customPriceStamp.setTimestamp(LocalDateTime.now());
        product.getOffers().clear();
        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
        offer.getPriceHistory().add(customPriceStamp);
        product.getOffers().add(offer);

        productRepository.save(product);
    }

    public void createProductWithShop(Shop shop) {
        var product = generate();
        product.getOffers().clear();
        var offer = new Offer(shop, faker.internet().url());

        var priceStamp = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), "PLN", Condition.NEW);
        offer.getPriceHistory().add(priceStamp);

        product.getOffers().add(offer);
        productRepository.save(product);
    }

    public void createProductWithName(String name) {
        var product = generate();
        product.setName(name);
        productRepository.save(product);
    }

    public record OfferPriceStamp(Shop shop, BigDecimal price, LocalDateTime timestamp) {}

    public Product createProductWithOffers(List<OfferPriceStamp> offerPriceStamps) {
        var product = generate();
        product.getOffers().clear();

        offerPriceStamps.forEach(offerPriceStamp -> {
            var priceStamp = new PriceStamp(offerPriceStamp.price(), "PLN", Condition.NEW);
            priceStamp.setTimestamp(offerPriceStamp.timestamp());

            var offer = product.getOffers().stream()
                    .filter(o -> o.getShop().equals(offerPriceStamp.shop))
                    .findFirst();

            if (offer.isPresent()) offer.get().getPriceHistory().add(priceStamp);
            else {
                var newOffer =
                        new Offer(offerPriceStamp.shop(), faker.internet().url());
                newOffer.getPriceHistory().add(priceStamp);
                product.getOffers().add(newOffer);
            }
        });

        return productRepository.save(product);
    }
}
