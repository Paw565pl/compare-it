package it.compare.backend.product.query;

import it.compare.backend.product.filter.ProductFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

@UtilityClass
public class ProductQueryBuilder {

    public Query buildQuery(ProductFilter filter) {
        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();

        getNameCriteria(filter).ifPresent(criteria::add);
        getCategoryNameCriteria(filter).ifPresent(criteria::add);
        getShopNameCriteria(filter).ifPresent(criteria::add);

        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        return query;
    }

    private Optional<Criteria> getNameCriteria(ProductFilter filter) {
        return Optional.ofNullable(filter.getName())
                .filter(StringUtils::hasText)
                .map(name -> Criteria.where("name").regex(name, "i"));
    }

    private Optional<Criteria> getCategoryNameCriteria(ProductFilter filter) {
        return Optional.ofNullable(filter.getCategory())
                .map(category -> Criteria.where("category").is(category));
    }

    private Optional<Criteria> getShopNameCriteria(ProductFilter filter) {
        return Optional.ofNullable(filter.getShop())
                .map(shop -> Criteria.where("offers.shop").is(shop));
    }
}
