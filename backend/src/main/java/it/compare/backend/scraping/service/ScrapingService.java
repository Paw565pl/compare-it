package it.compare.backend.scraping.service;

import it.compare.backend.product.model.Product;
import it.compare.backend.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrapingService {

    private final ProductRepository productRepository;

    public ScrapingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void createProductsOrAddPriceStamp(List<Product> scrapedProducts) {
        var products = new ArrayList<Product>();

        scrapedProducts.forEach(product -> {
            var existingProductOpt = productRepository.findByEan(product.getEan());

            if (existingProductOpt.isPresent()) {
                var existingProduct = existingProductOpt.get();
                var newOffer = product.getOffers().getFirst();
                var newPriceStamp = newOffer.getPriceHistory().getFirst();

                var offers = existingProduct.getOffers();

                // Check if there is an existing offer from the same shop or add a new one
                var offerFromGivenShop = offers.stream()
                        .filter(o -> o.getShop().equals(newOffer.getShop()))
                        .findFirst()
                        .orElse(newOffer);

                offerFromGivenShop.getPriceHistory().add(newPriceStamp);
                products.add(existingProduct);
            } else {
                products.add(product);
            }
        });

        products.forEach(productRepository::save);
    }
}
