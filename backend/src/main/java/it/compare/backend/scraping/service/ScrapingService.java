package it.compare.backend.scraping.service;

import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ScrapingService {

    private final ProductRepository productRepository;
    private final PriceAlertService priceAlertService;

    public ScrapingService(ProductRepository productRepository, PriceAlertService priceAlertService) {
        this.productRepository = productRepository;
        this.priceAlertService = priceAlertService;
    }

    @Transactional
    public void createProductsOrAddPriceStamp(List<Product> scrapedProducts) {
        var productsToSave = new ArrayList<Product>();

        var eanList = scrapedProducts.stream().map(Product::getEan).toList();
        var existingProductsMap = productRepository.findAllByEanIn(eanList).stream()
                .collect(Collectors.toMap(Product::getEan, product -> product));

        scrapedProducts.forEach(scrapedProduct -> {
            var existingProduct = existingProductsMap.get(scrapedProduct.getEan());

            // new product - immediately save it
            if (existingProduct == null) {
                productsToSave.add(scrapedProduct);
                return;
            }

            var newOffer = scrapedProduct.getOffers().getFirst();
            var newPriceStamp = newOffer.getPriceHistory().getFirst();

            var offers = existingProduct.getOffers();

            // check if there is an existing offer from the scraped shop
            var offerFromExistingShop = offers.stream()
                    .filter(o -> o.getShop().equals(newOffer.getShop()))
                    .findFirst();

            offerFromExistingShop.ifPresentOrElse(
                    offer -> offer.getPriceHistory().add(newPriceStamp), () -> offers.add(newOffer));
            productsToSave.add(existingProduct);
        });

        productRepository.saveAll(productsToSave);
        productsToSave.forEach(priceAlertService::checkPriceAlerts);
    }
}
