package it.compare.backend.scraping.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import it.compare.backend.pricealert.service.PriceAlertService;
import it.compare.backend.product.model.*;
import it.compare.backend.product.repository.ProductRepository;
import it.compare.backend.scraping.service.ScrapingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    private Product existingProduct1;
    private Product newProduct1;
    private Product newProduct2;

    private PriceStamp existingPriceStamp1;
    private PriceStamp existingPriceStamp2;
    private PriceStamp newPriceStamp;

    @BeforeEach
    void setUp() {
        newPriceStamp = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), "PLN", Condition.NEW);

        existingPriceStamp1 = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), "PLN", Condition.NEW);
        existingPriceStamp1.setTimestamp(LocalDateTime.now().minusDays(2));

        existingPriceStamp2 = new PriceStamp(BigDecimal.valueOf(faker.number().positive()), "PLN", Condition.NEW);
        existingPriceStamp2.setTimestamp(LocalDateTime.now().minusDays(3));

        var existingOffer1 = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
        existingOffer1.getPriceHistory().add(existingPriceStamp1);

        var existingOffer2 = new Offer(Shop.MORELE_NET, faker.internet().url());
        existingOffer2.getPriceHistory().add(existingPriceStamp2);

        existingProduct1 = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.PROCESSORS);
        existingProduct1.getOffers().addAll(List.of(existingOffer1, existingOffer2));

        var newOffer1 = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());
        newOffer1.getPriceHistory().add(newPriceStamp);

        newProduct1 = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.PROCESSORS);
        newProduct1.getOffers().add(newOffer1);

        var newOffer2 = new Offer(Shop.MEDIA_EXPERT, faker.internet().url());
        newOffer2.getPriceHistory().add(newPriceStamp);

        newProduct2 = new Product(PRODUCT_2_EAN, faker.commerce().productName(), Category.MOTHERBOARDS);
        newProduct2.getOffers().add(newOffer2);
    }

    @Test
    void shouldNotSaveIfListIsEmpty() {
        var scrapedProducts = List.<Product>of();
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(List.of());
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(any(Product.class));

        assertThat(productListCaptor.getValue(), (empty()));
    }

    @Test
    void shouldNotSaveIfProductsHasNoOffers() {
        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.PROCESSORS);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct1));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(any(Product.class));

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, (empty()));
        assertThat(existingProduct1.getOffers(), hasSize(2));

        var offer = existingProduct1.getOffers().stream()
                .filter(o -> o.getShop() == Shop.RTV_EURO_AGD)
                .findFirst()
                .orElseThrow();
        assertThat(offer.getPriceHistory(), hasSize(1));
        assertThat(offer.getPriceHistory(), contains(existingPriceStamp1));
    }

    @Test
    void shouldNotSaveIfOfferHasNoPriceStamps() {
        var offer = new Offer(Shop.RTV_EURO_AGD, faker.internet().url());

        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.PROCESSORS);
        product.getOffers().add(offer);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct1));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, never()).checkPriceAlerts(any(Product.class));

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, (empty()));
        assertThat(existingProduct1.getOffers(), hasSize(2));

        var existingOffer = existingProduct1.getOffers().stream()
                .filter(o -> o.getShop() == Shop.RTV_EURO_AGD)
                .findFirst()
                .orElseThrow();
        assertThat(existingOffer.getPriceHistory(), hasSize(1));
        assertThat(existingOffer.getPriceHistory(), contains(existingPriceStamp1));
    }

    @Test
    void shouldSaveNewProduct() {
        var scrapedProducts = List.of(newProduct2);
        var eans = List.of(PRODUCT_2_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of());
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(any(Product.class));

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));
        assertThat(savedProducts, contains(newProduct2));
    }

    @Test
    void shouldSavePriceStampIfProductExistsButOfferDoesNot() {
        var shopOffer = new Offer(Shop.MEDIA_EXPERT, faker.internet().url());
        shopOffer.getPriceHistory().add(newPriceStamp);

        var product = new Product(PRODUCT_1_EAN, faker.commerce().productName(), Category.PROCESSORS);
        product.getOffers().add(shopOffer);

        var scrapedProducts = List.of(product);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct1));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(any(Product.class));

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));

        var updatedProduct = savedProducts.getFirst();
        assertThat(updatedProduct, (sameInstance(existingProduct1)));
        assertThat(updatedProduct.getEan(), (equalTo(PRODUCT_1_EAN)));
        assertThat(updatedProduct.getOffers(), hasSize(3));
        assertThat(updatedProduct.getOffers(), hasItem(hasProperty("shop", equalTo(Shop.MEDIA_EXPERT))));

        var addedOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == Shop.MEDIA_EXPERT)
                .findFirst()
                .orElseThrow(() -> new AssertionError("new offer not found in existing product"));
        assertThat(addedOffer.getPriceHistory(), hasSize(1));
        assertThat(addedOffer.getPriceHistory(), contains(newPriceStamp));
    }

    @Test
    void shouldSavePriceStampIfProductAndOfferExist() {
        var scrapedProducts = List.of(newProduct1);
        var eans = List.of(PRODUCT_1_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct1));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(1)).checkPriceAlerts(any(Product.class));

        var savedProducts = productListCaptor.getValue();
        assertThat(savedProducts, hasSize(1));

        var updatedProduct = savedProducts.getFirst();
        assertThat(updatedProduct, (sameInstance(existingProduct1)));
        assertThat(updatedProduct.getEan(), (equalTo(PRODUCT_1_EAN)));
        assertThat(updatedProduct.getOffers(), hasSize(2));

        var updatedOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == Shop.RTV_EURO_AGD)
                .findFirst()
                .orElseThrow(() -> new AssertionError("updated offer not found in existing product"));

        assertThat(updatedOffer.getPriceHistory(), hasSize(2));
        assertThat(updatedOffer.getPriceHistory(), hasItems(existingPriceStamp1, newPriceStamp));

        var existingOffer = updatedProduct.getOffers().stream()
                .filter(o -> o.getShop() == Shop.MORELE_NET)
                .findFirst()
                .orElseThrow(() -> new AssertionError("existing offer not found in existing product"));
        assertThat(existingOffer.getPriceHistory(), hasSize(1));
        assertThat(existingOffer.getPriceHistory(), contains(existingPriceStamp2));
    }

    @Test
    void shouldProcessAndSaveMixedProductsCorrectly() {
        var scrapedProducts = List.of(newProduct1, newProduct2);
        var eans = List.of(PRODUCT_1_EAN, PRODUCT_2_EAN);

        when(productRepository.findAllByEanIn(eans)).thenReturn(List.of(existingProduct1));
        when(productRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        scrapingService.createProductsOrAddPriceStamp(scrapedProducts);

        verify(productRepository).findAllByEanIn(eans);
        verify(productRepository).saveAll(productListCaptor.capture());
        verify(priceAlertService, times(2)).checkPriceAlerts(any(Product.class));

        var savedOrUpdatedProducts = productListCaptor.getValue();
        assertThat(savedOrUpdatedProducts, hasSize(2));

        var updatedProduct1 = savedOrUpdatedProducts.stream()
                .filter(p -> p.getEan().equals(PRODUCT_1_EAN))
                .findFirst()
                .orElseThrow(() -> new AssertionError("updated product 1 not found in saved list"));
        assertThat(updatedProduct1, (sameInstance(existingProduct1)));

        var offer = updatedProduct1.getOffers().stream()
                .filter(o -> o.getShop() == Shop.RTV_EURO_AGD)
                .findFirst()
                .orElseThrow();
        assertThat(offer.getPriceHistory(), hasSize(2));
        assertThat(offer.getPriceHistory(), hasItems(existingPriceStamp1, newPriceStamp));

        var foundNewProduct2 = savedOrUpdatedProducts.stream()
                .filter(p -> p.getEan().equals(PRODUCT_2_EAN))
                .findFirst()
                .orElseThrow(() -> new AssertionError("new product 2 not found in saved list"));
        assertThat(foundNewProduct2, (sameInstance(this.newProduct2)));
        assertThat(foundNewProduct2.getOffers(), hasSize(1));
        assertThat(foundNewProduct2.getOffers().getFirst().getShop(), (equalTo(Shop.MEDIA_EXPERT)));
        assertThat(foundNewProduct2.getOffers().getFirst().getPriceHistory(), contains(newPriceStamp));
    }
}
