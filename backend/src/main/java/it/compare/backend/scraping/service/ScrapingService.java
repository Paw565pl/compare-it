package it.compare.backend.scraping.service;

import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.ComputedState;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapingService {

    private final ProductRepository productRepository;
    private final PriceAlertService priceAlertService;

    @Transactional
    public void createProductsOrAddPriceStamp(List<Product> scrapedProducts) {
        var eanList = scrapedProducts.stream().map(Product::getEan).toList();
        var existingProductsEanMap = productRepository.findAllByEanIn(eanList).stream()
                .collect(Collectors.toMap(Product::getEan, Function.identity()));

        var products = scrapedProducts.stream()
                .map(scrapedProduct -> {
                    var existingProduct = existingProductsEanMap.get(scrapedProduct.getEan());

                    // new product - immediately save it
                    if (existingProduct == null) {
                        var computedState = ComputedState.fromProduct(scrapedProduct);
                        scrapedProduct.setComputedState(computedState);

                        return scrapedProduct;
                    }

                    if (scrapedProduct.getOffers().isEmpty()) return null;
                    var newOffer = scrapedProduct.getOffers().getFirst();

                    if (newOffer.getPriceHistory().isEmpty()) return null;
                    var newPriceStamp = newOffer.getPriceHistory().getFirst();

                    var offers = existingProduct.getOffers();

                    // check if there is an existing offer from the scraped shop
                    var offerFromExistingShop = offers.stream()
                            .filter(o -> o.getShop().equals(newOffer.getShop()))
                            .findFirst();

                    offerFromExistingShop.ifPresentOrElse(
                            offer -> offer.getPriceHistory().add(newPriceStamp), () -> offers.add(newOffer));

                    var computedState = ComputedState.fromProduct(existingProduct);
                    existingProduct.setComputedState(computedState);

                    return existingProduct;
                })
                .filter(Objects::nonNull)
                .toList();

        if (!products.isEmpty()) {
            productRepository.saveAll(products);
            priceAlertService.checkPriceAlerts(products);
        }

        log.info("saved {} products", products.size());
    }
}
