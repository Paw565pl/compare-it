package it.compare.backend.product.criteria;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import java.util.Arrays;
import lombok.*;
import org.springframework.data.mongodb.core.query.Criteria;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {
    private String name;
    private String category;
    private String shop;

    public Criteria toCriteria() {
        Criteria criteria = new Criteria();

        if (name != null) {
            criteria.and("name").regex(name, "i");
        }

        if (category != null) {
            Category matchingCategory = Arrays.stream(Category.values())
                    .filter(c -> c.getHumanReadableName().equalsIgnoreCase(category))
                    .findFirst()
                    .orElse(null);

            criteria.and("category").is(matchingCategory != null ? matchingCategory : "NON_EXISTENT_CATEGORY");
        }

        if (shop != null) {
            Shop matchingShop = Arrays.stream(Shop.values())
                    .filter(s -> s.getHumanReadableName().equalsIgnoreCase(shop))
                    .findFirst()
                    .orElse(null);

            criteria.and("offers.shop").is(matchingShop != null ? matchingShop : "NON_EXISTENT_SHOP");
        }

        return criteria;
    }
}
