package it.compare.backend.scraping.mediaexpert.scraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.compare.backend.product.model.*;
import it.compare.backend.scraping.mediaexpert.dto.MediaExpertSparkStateDto;
import it.compare.backend.scraping.scraper.ScraperWorker;
import it.compare.backend.scraping.util.ScrapingUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
class MediaExpertScraperWorker implements ScraperWorker {

    private static final Shop CURRENT_SHOP = Shop.MEDIA_EXPERT;
    private static final String BASE_URL = "https://www.mediaexpert.pl";
    private static final int LIMIT = 50;

    private final ObjectMapper objectMapper;
    private final ObjectFactory<WebDriver> webDriverFactory;
    private final Semaphore mediaExpertSemaphore;

    @Async
    @Override
    public CompletableFuture<List<Product>> scrapeCategory(Category category, String categoryLocator) {
        var isSemaphorePermitAcquired = false;
        WebDriver webDriverInstance = null;

        try {
            mediaExpertSemaphore.acquire();
            isSemaphorePermitAcquired = true;

            var webDriver = webDriverFactory.getObject();
            webDriverInstance = webDriver;

            var productUrls = new ArrayList<String>();

            var numberOfPages = getNumberOfPages(webDriver, categoryLocator);
            log.debug("available pages: {} - category {}", numberOfPages, category);

            for (var currentPage = 1; currentPage <= numberOfPages; currentPage++) {
                var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                        .path(categoryLocator)
                        .queryParam("page", currentPage)
                        .queryParam("limit", LIMIT)
                        .build()
                        .toUri();
                log.debug("scraping page {} - category {}", currentPage, category);

                webDriver.get(uri.toString());
                var pageSource = webDriver.getPageSource();
                if (pageSource == null) continue;

                var document = Jsoup.parse(pageSource);

                var productList = document.select("div.offers-list");
                var currentPageProductUrls = productList.select("h2.name a").stream()
                        .map(link -> link.attr("href"))
                        .toList();
                productUrls.addAll(currentPageProductUrls);

                ScrapingUtil.sleep();
            }

            var products = productUrls.stream()
                    .map(url -> {
                        log.debug(
                                "scraping product nr {} of {} in category {}",
                                productUrls.indexOf(url) + 1,
                                productUrls.size(),
                                category);
                        return scrapeProduct(webDriver, category, url);
                    })
                    .filter(Objects::nonNull)
                    .toList();
            return CompletableFuture.completedFuture(products);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("thread was interrupted while acquiring semaphore for scraping category {}", category);

            return CompletableFuture.failedFuture(e);
        } finally {
            if (webDriverInstance != null) webDriverInstance.quit();
            if (isSemaphorePermitAcquired) mediaExpertSemaphore.release();
        }
    }

    @Override
    public Shop getShop() {
        return CURRENT_SHOP;
    }

    private int getNumberOfPages(WebDriver webDriver, String categoryLocator) {
        var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(categoryLocator)
                .queryParam("limit", LIMIT)
                .build()
                .toUri();

        try {
            webDriver.get(uri.toString());
            var lastPageElement = new WebDriverWait(webDriver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(new ByCssSelector(".pagination .from")));

            return Optional.of(lastPageElement.getText())
                    .map(text -> text.trim().replaceAll("\\D", ""))
                    .filter(text -> !text.isBlank())
                    .map(Integer::parseInt)
                    .orElse(1);
        } catch (TimeoutException e) {
            log.warn("timeout while waiting for total pages element to be visible at uri {}", uri);
        } catch (Exception e) {
            log.error(
                    "unexpected error of {} has occurred while getting number of pages from uri {} - {}",
                    e.getClass(),
                    uri,
                    e.getMessage());
        }

        return 1;
    }

    private Product scrapeProduct(WebDriver webDriver, Category category, String productHref) {
        var uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(productHref)
                .build()
                .toUri();
        log.debug("scraping product from uri: {}", uri);

        try {
            webDriver.get(uri.toString());
            var pageSource = webDriver.getPageSource();
            if (pageSource == null) return null;

            var document = Jsoup.parse(pageSource);

            var sparkScriptXpath = "//*[@id=\"state\"]";
            new WebDriverWait(webDriver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(new ByXPath(sparkScriptXpath)));

            var sparkScriptElement = document.selectXpath(sparkScriptXpath);
            var sparkId = !sparkScriptElement.dataNodes().isEmpty()
                    ? sparkScriptElement.dataNodes().getFirst().getWholeData().trim()
                    : null;
            if (sparkId == null || sparkId.isBlank()) {
                log.debug("spark id is null or empty for product uri: {}", uri);
                return null;
            }

            var ean = getEan(webDriver, sparkId);
            if (ean == null || ean.isBlank()) {
                log.debug("ean is null or empty for product uri: {}", uri);
                return null;
            }

            var name = document.select("h1.name").text().trim();

            var priceElements = document.select("div.main-price.is-big");
            var priceElement =
                    !priceElements.isEmpty() ? priceElements.getFirst().text().trim() : null;
            var priceWithoutSymbol = priceElement != null ? priceElement.replaceAll("\\D", "") : null;
            var price = (priceWithoutSymbol != null && !priceWithoutSymbol.isEmpty())
                    ? new BigDecimal(priceWithoutSymbol).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : null;
            if (price == null) return null;

            var promoCodeElement = document.select("div.promo-code");
            var promoCode = !promoCodeElement.isEmpty()
                    ? promoCodeElement.getFirst().text().trim()
                    : null;

            var images = document.select("div#section_product-gallery-thumbs img").stream()
                    .map(image -> image.attr("src").trim().replace("gallery_100_100", "gallery_500_500"))
                    .toList();
            if (images.isEmpty())
                images = document.select(".product-gallery-view img").stream()
                        .map(image -> image.attr("src").trim())
                        .toList();

            var condition = getCondition(document);
            var priceStamp = new PriceStamp(price, "PLN", condition);
            priceStamp.setPromoCode(promoCode);

            var offer = new Offer(CURRENT_SHOP, uri.toString());
            offer.getPriceHistory().add(priceStamp);

            var product = new Product(ean, name, category);
            product.getImages().addAll(images);
            product.getOffers().add(offer);

            log.debug("scraped product: {}", product);
            ScrapingUtil.sleep();

            return product;
        } catch (TimeoutException e) {
            log.warn(
                    "timeout while waiting for spark script element to be present in category {} at uri {}",
                    category,
                    uri);
        } catch (Exception e) {
            log.error(
                    "unexpected error of {} has occurred while scraping single product from category {} from uri {} - {}",
                    e.getClass(),
                    category,
                    uri,
                    e.getMessage());
        }

        return null;
    }

    private String getEan(WebDriver webDriver, String sparkId) {
        var path = "/spark-state/" + sparkId;
        var uri =
                UriComponentsBuilder.fromUriString(BASE_URL).path(path).build().toUri();

        webDriver.get(uri.toString());
        var pageSource = webDriver.getPageSource();
        if (pageSource == null) return null;

        var json = Jsoup.parse(pageSource).select("pre").text().trim();

        try {
            var sparkStateDto = objectMapper.readValue(json, MediaExpertSparkStateDto.class);
            var ean = sparkStateDto.productShowService().offer().systemAttributes().stream()
                    .filter(systemAttribute -> systemAttribute.id() == 22)
                    .findFirst();

            return ean.map(systemAttribute -> systemAttribute.values().getFirst())
                    .orElse(null);
        } catch (JsonProcessingException e) {
            log.warn("json deserialization error from uri {} - {}", uri, e.getMessage());
            return null;
        }
    }

    private Condition getCondition(Document document) {
        var aboveNameEmblem = document.selectFirst(".above-name-emblem");
        if (aboveNameEmblem == null) return Condition.NEW;

        var img = aboveNameEmblem.selectFirst("img");
        var isOutlet = Optional.ofNullable(img)
                .map(element -> element.attr("src"))
                .filter(src -> !src.isEmpty())
                .map(src -> src.toLowerCase().contains("outlet"))
                .orElse(false);

        return Boolean.TRUE.equals(isOutlet) ? Condition.OUTLET : Condition.NEW;
    }
}
