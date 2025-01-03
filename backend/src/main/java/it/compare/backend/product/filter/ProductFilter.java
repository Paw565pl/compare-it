package it.compare.backend.product.filter;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import lombok.Data;

@Data
public class ProductFilter {
    private String name;
    private Category category;
    private Shop shop;
}
