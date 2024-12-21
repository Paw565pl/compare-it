package it.compare.backend.product.response;

import it.compare.backend.product.model.Category;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ProductListResponse {
    private String id;
    private String name;
    private String ean;
    private Category category;
    private String mainImageUrl;
    private BigDecimal lowestCurrentPrice;
    private String lowestPriceShop;
    private Long offerCount;
    private Boolean isAvailable;
}
