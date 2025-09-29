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
import java.util.List;
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
        var user = userTestDataFactory.createOne();

        var targetPrice = BigDecimal.valueOf(faker.number().randomDouble(2, 50, 5000));

        return new PriceAlert(user, product, targetPrice, true);
    }

    @Override
    public PriceAlert createOne() {
        return priceAlertRepository.save(generate());
    }

    @Override
    public List<PriceAlert> createMany(int count) {
        var alerts = new ArrayList<PriceAlert>();
        for (int i = 0; i < count; i++) {
            alerts.add(generate());
        }

        return priceAlertRepository.saveAll(alerts);
    }

    @Override
    public void clear() {
        priceAlertRepository.deleteAll();
    }

    public PriceAlert createPriceAlertForUser(User user) {
        var product = productTestDataFactory.createOne();
        var alert = new PriceAlert(
                user,
                product,
                product.getOffers().getFirst().getPriceHistory().getFirst().getPrice(),
                faker.bool().bool());

        return priceAlertRepository.save(alert);
    }

    public PriceAlert createPriceAlertWithUserAndProduct(User user, Product product) {
        var alert = new PriceAlert(
                user,
                product,
                product.getOffers().getFirst().getPriceHistory().getFirst().getPrice(),
                faker.bool().bool());

        return priceAlertRepository.save(alert);
    }

    public PriceAlert createPriceAlertWithActiveStatus(User user, boolean active) {
        var alert = createPriceAlertForUser(user);
        alert.setIsActive(active);

        return priceAlertRepository.save(alert);
    }
}
