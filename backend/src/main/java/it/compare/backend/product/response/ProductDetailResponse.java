package it.compare.backend.product.response;

import it.compare.backend.product.model.Category;
import java.util.List;
import lombok.Data;

@Data
public class ProductDetailResponse {
    private String id;
    private String ean;
    private String name;
    private Category category;
    private List<String> images;
    private List<OfferResponse> offers;
}
