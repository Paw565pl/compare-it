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

            if (existingProduct != null) {
                var newOffer = scrapedProduct.getOffers().getFirst();
                var newPriceStamp = newOffer.getPriceHistory().getFirst();

                var offers = existingProduct.getOffers();

                // Check if there is an existing offer from the same shop or add a new one
                var offerFromGivenShop = offers.stream()
                        .filter(o -> o.getShop().equals(newOffer.getShop()))
                        .findFirst()
                        .orElse(newOffer);

                offerFromGivenShop.getPriceHistory().add(newPriceStamp);
                productsToSave.add(existingProduct);
            } else {
                productsToSave.add(scrapedProduct);
            }
        });
    //TODO: Czemu nie zapisuje
//        productsToSave.forEach(product -> {
//            var savedProduct = productRepository.save(product);
//            priceAlertService.checkPriceAlerts(savedProduct);
//        });
        productsToSave.forEach(productRepository::save);
    }
}
