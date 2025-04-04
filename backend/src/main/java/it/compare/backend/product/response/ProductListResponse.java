package it.compare.backend.product.response;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProductListResponse {
    private String id;
    private String name;
    private String ean;
    private Category category;

    @Nullable private String mainImageUrl;

    @Nullable private BigDecimal lowestCurrentPrice;

    @Nullable private String lowestPriceCurrency;

    @Nullable private Shop lowestPriceShop;

    private Long offersCount;
    private Boolean isAvailable;
}
