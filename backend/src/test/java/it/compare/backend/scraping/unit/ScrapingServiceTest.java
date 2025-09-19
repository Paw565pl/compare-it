package it.compare.backend.scraping.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.*;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.scraping.service.ScrapingService;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScrapingServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceAlertService priceAlertService;

    @InjectMocks
    private ScrapingService scrapingService;

    @Captor
    private ArgumentCaptor<List<Product>> productListCaptor;

    private final Faker faker = new Faker();

    private static final String PRODUCT_1_EAN = "123";
    private static final String PRODUCT_2_EAN = "456";

    private Product existingProduct;
    private Product newProduct1;
    private Product newProduct2;

    private Offer existingOfferFromShopA;
    private Offer existingOfferFromShopB;
    private Offer newOfferFromShopA;
    private Offer newOfferFromShopC;

    private PriceStamp existingPriceStamp1;
    private PriceStamp existingPriceStamp2;
    private PriceStamp newPriceStamp;

    @BeforeEach
    void setUp() {
        existingPriceStamp1 =
                new PriceStamp(BigDecimal.valueOf(faker.number().positive()), Currency.PLN, Condition.NEW);
        existingPriceStamp1.setTimestamp(Instant.now().minus(Duration.ofDays(2)));

        existingPriceStamp2 =
                new PriceStamp(BigDecimal.valueOf(faker.number().positive()), Currency.PLN, Condition.NEW);
        existingPriceStamp2.setTimestamp(Instant.now().minus(Duration.ofDays(3)));

        existingOfferFromShopA = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
        existingOfferFromShopA.getPriceHistory().add(existingPriceStamp1);

        existingOfferFromShopB = new Offer(Shop.MORELE_NET, faker.internet().url());
        existingOfferFromShopB.getPriceHistory().add(existingPriceStamp2);

        existingProduct = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.CPU);
        existingProduct.getOffers().addAll(List.of(existingOfferFromShopA, existingOfferFromShopB));

        newPriceStamp = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), Currency.PLN, Condition.NEW);

        newOfferFromShopA = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
        newOfferFromShopA.getPriceHistory().add(newPriceStamp);

        newProduct1 = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.CPU);
        newProduct1.getOffers().add(newOfferFromShopA);

        newOfferFromShopC = new Offer(Shop.MEDIA_EXPERT, faker.internet().url());
        newOfferFromShopC.getPriceHistory().add(newPriceStamp);

        newProduct2 = new Product(PRODUCT_2_EAN, faker.commerce().productName(), Category.MOTHERBOARD);
        newProduct2.getOffers().add(newOfferFromShopC);
    }

    @Test
    void shouldNotSaveIfListIsEmpty() {
        var scrapedProducts = List.<Product>of();
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(List.of());
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(anyList());

        assertThat(productListCaptor.getValue(), (empty()));
    }

    @Test
    void shouldNotSaveIfProductHasNoOffers() {
        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.CPU);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(anyList());

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, (empty()));
        assertThat(existingProduct.getOffers(), hasSize(2));
    }

    @Test
    void shouldNotSaveIfOfferHasNoPriceStamps() {
        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());

        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.CPU);
        product.getOffers().add(offer);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(anyList());

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, (empty()));
        assertThat(existingProduct.getOffers(), hasSize(2));
    }

    @Test
    void shouldSaveNewProduct() {
        var scrapedProducts = List.of(newProduct1);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of());
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(anyList());

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));
        assertThat(savedProducts, contains(newProduct1));
        assertThat(savedProducts.getFirst().getOffers(), contains(newOfferFromShopA));
    }

    @Test
    void shouldSavePriceStampIfProductExistsButOfferDoesNot() {
        var offer = new Offer(Shop.MEDIA_EXPERT, faker.internet().url());
        offer.getPriceHistory().add(newPriceStamp);

        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.CPU);
        product.getOffers().add(offer);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(anyList());

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));

        var updatedProduct = savedProducts.getFirst();
        assertThat(updatedProduct, (sameInstance(existingProduct)));
        assertThat(updatedProduct.getEan(), (equalTo(PRODUCT_1_EAN)));
        assertThat(updatedProduct.getOffers(), hasSize(3));
        assertThat(updatedProduct.getOffers(), hasItem(hasProperty("shop", equalTo(newOfferFromShopC.getShop()))));

        var addedOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == newOfferFromShopC.getShop())
                .findFirst()
                .orElseThrow(() -> new AssertionError("new offer not found in existing product"));
        assertThat(addedOffer.getPriceHistory(), hasSize(1));
        assertThat(addedOffer.getPriceHistory(), contains(newPriceStamp));
    }

    @Test
    void shouldSavePriceStampIfProductAndOfferExist() {
        var scrapedProducts = List.of(newProduct1);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(anyList());

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));

        var updatedProduct = savedProducts.getFirst();
        assertThat(updatedProduct, (sameInstance(existingProduct)));
        assertThat(updatedProduct.getEan(), (equalTo(PRODUCT_1_EAN)));
        assertThat(updatedProduct.getOffers(), hasSize(2));

        var updatedOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == existingOfferFromShopA.getShop())
                .findFirst()
                .orElseThrow(() -> new AssertionError("updated offer not found in existing product"));

        assertThat(updatedOffer.getPriceHistory(), hasSize(2));
        assertThat(updatedOffer.getPriceHistory(), hasItems(existingPriceStamp1, newPriceStamp));

        var existingOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == existingOfferFromShopB.getShop())
                .findFirst()
                .orElseThrow(() -> new AssertionError("existing offer not found in existing product"));
        assertThat(existingOffer.getPriceHistory(), hasSize(1));
        assertThat(existingOffer.getPriceHistory(), contains(existingPriceStamp2));
    }

    @Test
    void shouldProcessAndSaveMixedProductsCorrectly() {
        var scrapedProducts = List.of(newProduct1, newProduct2);
        var eans = List.of(PRODUCT_1_EAN, PRODUCT_2_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService).checkPriceAlerts(anyList());

        var savedOrUpdatedProducts = productListCaptor.getValue();
        assertThat(savedOrUpdatedProducts, hasSize(2));

        var updatedProduct1 = savedOrUpdatedProducts.stream()
                .filter(p -> p.getEan().equals(PRODUCT_1_EAN))
                .findFirst()
                .orElseThrow(() -> new AssertionError("updated product 1 not found in saved list"));
        assertThat(updatedProduct1, (sameInstance(existingProduct)));

        var updatedOffer = updatedProduct1.getOffers().stream()
                .filter(o -> o.getShop() == existingOfferFromShopA.getShop())
                .findFirst()
                .orElseThrow();
        assertThat(updatedOffer.getPriceHistory(), hasSize(2));
        assertThat(updatedOffer.getPriceHistory(), hasItems(existingPriceStamp1, newPriceStamp));

        var foundNewProduct2 = savedOrUpdatedProducts.stream()
                .filter(p -> p.getEan().equals(PRODUCT_2_EAN))
                .findFirst()
                .orElseThrow(() -> new AssertionError("new product 2 not found in saved list"));
        assertThat(foundNewProduct2, (sameInstance(newProduct2)));
        assertThat(foundNewProduct2.getOffers(), hasSize(1));
        assertThat(foundNewProduct2.getOffers().getFirst().getShop(), (equalTo(newOfferFromShopC.getShop())));
        assertThat(foundNewProduct2.getOffers().getFirst().getPriceHistory(), contains(newPriceStamp));
    }
}
