package it.compare.backend.product.scheduler;

import it.compare.backend.product.model.ComputedState;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductComputedStateScheduler {

    private final ProductComputedStateRefreshComponent productComputedStateRefreshComponent;

    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        log.info("started product computed state scheduler job");
        productComputedStateRefreshComponent.recalculateStateForStaleProducts();
        log.info("finished product computed state scheduler job");
    }
}

@Component
@RequiredArgsConstructor
class ProductComputedStateRefreshComponent {

    private static final int BATCH_SIZE = 500;

    private final ProductRepository productRepository;

    @Async
    public void recalculateStateForStaleProducts() {
        var cutOff = Instant.now().minus(ComputedState.AVAILABILITY_DAYS_THRESHOLD);

        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        Page<Product> page;

        do {
            page = productRepository.findAllByUpdatedAtBefore(cutOff, pageable);
            var products = page.getContent().stream()
                    .map(product -> {
                        var computedState = ComputedState.fromProduct(product);
                        product.setComputedState(computedState);

                        return product;
                    })
                    .toList();

            if (!products.isEmpty()) productRepository.saveAll(products);
            pageable = page.nextPageable();
        } while (page.hasNext());
    }
}
