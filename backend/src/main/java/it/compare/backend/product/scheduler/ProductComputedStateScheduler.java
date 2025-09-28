package it.compare.backend.product.scheduler;

import it.compare.backend.product.model.ComputedState;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductComputedStateScheduler {

    private final ProductComputedStateRefreshComponent productComputedStateRefreshComponent;

    @Scheduled(cron = "0 0 3 * * *")
    public void run() {
        productComputedStateRefreshComponent.recalculateStateForStaleProducts();
    }
}

@Slf4j
@Component
@RequiredArgsConstructor
class ProductComputedStateRefreshComponent {

    private static final int BATCH_SIZE = 500;

    private final ProductRepository productRepository;

    @Async
    public void recalculateStateForStaleProducts() {
        log.info("started recalculating state for stale products");

        var cutOff = Instant.now().minus(ComputedState.AVAILABILITY_DAYS_THRESHOLD);
        var pageable = PageRequest.of(0, BATCH_SIZE, Sort.by("_id").ascending());
        Slice<Product> products;

        do {
            products = productRepository.findAllByUpdatedAtBefore(cutOff, pageable);
            products.forEach(product -> product.setComputedState(ComputedState.fromProduct(product)));

            productRepository.saveAll(products);
        } while (products.hasContent());

        log.info("finished recalculating state for stale products");
    }
}
