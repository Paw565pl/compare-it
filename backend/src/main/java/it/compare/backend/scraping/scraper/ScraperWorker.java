package it.compare.backend.scraping.scraper;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Product;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScraperWorker {
    CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryLocator);
}
