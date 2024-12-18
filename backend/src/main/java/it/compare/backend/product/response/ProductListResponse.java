package it.compare.backend.product.response;

import it.compare.backend.product.model.Category;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductListResponse {
    private String id;
    private String name;
    private Category category;
    private String mainImageUrl;
    private BigDecimal lowestCurrentPrice;
    private String lowestPriceShopName;
    private int offerCount;
    private Boolean isAvailable;
}
