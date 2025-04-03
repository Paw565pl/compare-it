package it.compare.backend.pricealert.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.pricealert.respository.PriceAlertRepository;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.product.model.Product;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import net.datafaker.Faker;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import(FakerConfig.class)
public class PriceAlertTestDataFactory implements TestDataFactory<PriceAlert> {

    private final Faker faker;
    private final PriceAlertRepository priceAlertRepository;
    private final ProductTestDataFactory productTestDataFactory;
    private final UserTestDataFactory userTestDataFactory;

    public PriceAlertTestDataFactory(
            Faker faker,
            PriceAlertRepository priceAlertRepository,
            ProductTestDataFactory productTestDataFactory,
            UserTestDataFactory userTestDataFactory) {
        this.faker = faker;
        this.priceAlertRepository = priceAlertRepository;
        this.productTestDataFactory = productTestDataFactory;
        this.userTestDataFactory = userTestDataFactory;
    }

    @Override
    public PriceAlert generate() {
        var product = productTestDataFactory.createOne();
        User user = userTestDataFactory.createOne();

        var targetPrice = BigDecimal.valueOf(faker.number().randomDouble(2, 50, 5000));

        PriceAlert priceAlert = new PriceAlert(product, targetPrice);
        priceAlert.setUser(user);
        priceAlert.setIsOutletAllowed(true);

        return priceAlert;
    }

    @Override
    public PriceAlert createOne() {
        return priceAlertRepository.save(generate());
    }

    @Override
    public Collection<PriceAlert> createMany(int count) {
        var alerts = new ArrayList<PriceAlert>();
        for (int i = 0; i < count; i++) {
            alerts.add(createOne());
        }
        return alerts;
    }

    @Override
    public void clear() {
        priceAlertRepository.deleteAll();
    }

    public PriceAlert createPriceAlertForUser(User user) {
        var product = productTestDataFactory.createOne();
        var alert = new PriceAlert(
                product,
                product.getOffers().getFirst().getPriceHistory().getFirst().getPrice());
        alert.setUser(user);
        alert.setIsOutletAllowed(faker.bool().bool());
        return priceAlertRepository.save(alert);
    }

    public PriceAlert createPriceAlertWithUserAndProduct(User user, Product product) {
        var alert = new PriceAlert(
                product,
                product.getOffers().getFirst().getPriceHistory().getFirst().getPrice());
        alert.setUser(user);
        alert.setIsOutletAllowed(faker.bool().bool());
        return priceAlertRepository.save(alert);
    }

    public PriceAlert createPriceAlertWithActiveStatus(User user, boolean active) {
        PriceAlert alert = createPriceAlertForUser(user);
        alert.setActive(active);
        return priceAlertRepository.save(alert);
    }
}
