package it.compare.backend.product.criteria;

import io.micrometer.common.util.StringUtils;
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

        if (StringUtils.isNotBlank(name)) {
            criteria.and("name").regex(name, "i");
        }

        if (StringUtils.isNotBlank(category)) {
            criteria.and("category").is(category.toUpperCase());
        }

        if (StringUtils.isNotBlank(shop)) {
            criteria.and("offers.shop").is(shop.toUpperCase());
        }

        return criteria;
    }
}
