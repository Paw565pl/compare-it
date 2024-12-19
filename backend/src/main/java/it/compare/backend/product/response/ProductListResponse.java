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
    private Category category;
    private String mainImageUrl;
    private BigDecimal lowestCurrentPrice;
    private String lowestPriceShop;
    private Integer offerCount;
    private Boolean isAvailable;
}
