package it.compare.backend.pricealert.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import it.compare.backend.pricealert.model.PriceAlert;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class PriceAlertRepositoryTest extends PriceAlertTest {

    @Autowired
    private UserTestDataFactory userTestDataFactory;

    @Test
    void shouldFindAllByUserId() {
        var user1 = userTestDataFactory.createOne();
        var user2 = userTestDataFactory.createOne();

        var alert1 = priceAlertTestDataFactory.createPriceAlertForUser(user1);
        var alert2 = priceAlertTestDataFactory.createPriceAlertForUser(user1);
        var alert3 = priceAlertTestDataFactory.createPriceAlertForUser(user2);

        var pageable = PageRequest.of(0, 10);

        var alerts = priceAlertRepository.findAllByUserId(user1.getId(), pageable);

        assertThat(alerts.getContent(), hasSize(2));
        assertThat(
                alerts.getContent().stream().map(PriceAlert::getId).toList(),
                containsInAnyOrder(alert1.getId(), alert2.getId()));
        assertThat(alerts.getContent().stream().map(PriceAlert::getId).toList(), not(contains(alert3.getId())));
    }

    @Test
    void shouldFindAllByUserIdAndActive() {
        var user = userTestDataFactory.createOne();

        var activeAlert1 = priceAlertTestDataFactory.createPriceAlertForUser(user);
        var activeAlert2 = priceAlertTestDataFactory.createPriceAlertForUser(user);
        var inactiveAlert = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(user, false);

        var pageable = PageRequest.of(0, 10);

        var activeAlerts = priceAlertRepository.findAllByUserIdAndIsActive(user.getId(), true, pageable);
        var inactiveAlerts = priceAlertRepository.findAllByUserIdAndIsActive(user.getId(), false, pageable);

        assertThat(activeAlerts.getContent(), hasSize(2));
        assertThat(
                activeAlerts.getContent().stream().map(PriceAlert::getId).toList(),
                containsInAnyOrder(activeAlert1.getId(), activeAlert2.getId()));

        assertThat(inactiveAlerts.getContent(), hasSize(1));
        assertThat(
                inactiveAlerts.getContent().stream().map(PriceAlert::getId).toList(), contains(inactiveAlert.getId()));
    }

    @Test
    void shouldCheckExistsByUserIdAndProductIdAndActiveTrue() {
        var user = userTestDataFactory.createOne();
        var product = productTestDataFactory.createOne();

        var alert = priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product);

        assertThat(
                priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(user.getId(), product.getId()),
                is(true));

        alert.setIsActive(false);
        priceAlertRepository.save(alert);

        assertThat(
                priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(user.getId(), product.getId()),
                is(false));

        var anotherUser = userTestDataFactory.createOne();
        priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(anotherUser, product);

        assertThat(
                priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(user.getId(), product.getId()),
                is(false));

        assertThat(
                priceAlertRepository.existsByUserIdAndProductIdAndIsActiveTrue(anotherUser.getId(), product.getId()),
                is(true));
    }

    @Test
    void shouldDeleteAllByUserIdAndIsActiveFalse() {
        var user = userTestDataFactory.createOne();

        var activeAlert = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(user, true);
        var inactiveAlert1 = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(user, false);
        var inactiveAlert2 = priceAlertTestDataFactory.createPriceAlertWithActiveStatus(user, false);

        priceAlertRepository.deleteAllByUserIdAndIsActiveFalse(user.getId());

        assertThat(priceAlertRepository.existsById(inactiveAlert1.getId()), is(false));
        assertThat(priceAlertRepository.existsById(inactiveAlert2.getId()), is(false));
        assertThat(priceAlertRepository.existsById(activeAlert.getId()), is(true));
    }

    @Test
    void shouldReturnAllByUserIdAndProductId() {
        var user = userTestDataFactory.createOne();
        var product1 = productTestDataFactory.createOne();
        var product2 = productTestDataFactory.createOne();

        var alert1 = priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product1);
        priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product2);

        var pageable = PageRequest.of(0, 10);

        var alerts = priceAlertRepository.findAllByUserIdAndProductId(user.getId(), product1.getId(), pageable);

        assertThat(alerts.getContent(), hasSize(1));
        assertThat(alerts.getContent().getFirst().getId(), is(alert1.getId()));
    }

    @Test
    void shouldReturnAllByUserIdAndProductIdAndIsActive() {
        var user = userTestDataFactory.createOne();
        var product = productTestDataFactory.createOne();

        var activeAlert1 = priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product);
        var activeAlert2 = priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product);
        var inactiveAlert = priceAlertTestDataFactory.createPriceAlertWithUserAndProduct(user, product);
        inactiveAlert.setIsActive(false);
        priceAlertRepository.save(inactiveAlert);

        var pageable = PageRequest.of(0, 10);

        var activeAlerts = priceAlertRepository.findAllByUserIdAndProductIdAndIsActive(
                user.getId(), product.getId(), true, pageable);
        var inactiveAlerts = priceAlertRepository.findAllByUserIdAndProductIdAndIsActive(
                user.getId(), product.getId(), false, pageable);

        assertThat(activeAlerts.getContent(), hasSize(2));
        assertThat(
                activeAlerts.getContent().stream().map(PriceAlert::getId).toList(),
                containsInAnyOrder(activeAlert1.getId(), activeAlert2.getId()));

        assertThat(inactiveAlerts.getContent(), hasSize(1));
        assertThat(
                inactiveAlerts.getContent().stream().map(PriceAlert::getId).toList(), contains(inactiveAlert.getId()));
    }
}
