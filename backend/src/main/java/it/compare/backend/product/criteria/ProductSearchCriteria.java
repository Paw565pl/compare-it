package it.compare.backend.product.criteria;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.ProductListResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {
    private String name;
    private String category;
    private String shop;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Pageable pageable;
    private static final String PRICE_FIELD = "lowestCurrentPrice";

    public boolean requiresInMemoryProcessing() {
        return (minPrice != null || maxPrice != null)
                || (pageable.getSort().isSorted()
                && pageable.getSort().stream()
                .anyMatch(order -> order.getProperty().equals(PRICE_FIELD)));
    }

    public Query toQuery() {
        var query = Query.query(toCriteria());

        if (pageable.getSort().isSorted()) {
            var orders = pageable.getSort().stream()
                    .filter(order -> !order.getProperty().equals(PRICE_FIELD))
                    .map(order -> new Sort.Order(
                            order.getDirection(), order.getProperty().toLowerCase()))
                    .toList();

            if (!orders.isEmpty()) {
                query.with(Sort.by(orders));
            }
        }

        if (!requiresInMemoryProcessing()) {
            query.with(pageable);
        }

        return query;
    }

    public Criteria toCriteria() {
        var criteria = new Criteria();

        if (name != null) {
            criteria.and("name").regex(name, "i");
        }

        if (category != null) {
            var matchingCategory = Arrays.stream(Category.values())
                    .filter(c -> c.getHumanReadableName().equalsIgnoreCase(category))
                    .findFirst()
                    .orElse(null);

            criteria.and("category").is(matchingCategory != null ? matchingCategory : "NON_EXISTENT_CATEGORY");
        }

        if (shop != null) {
            var matchingShop = Arrays.stream(Shop.values())
                    .filter(s -> s.getHumanReadableName().equalsIgnoreCase(shop))
                    .findFirst()
                    .orElse(null);

            criteria.and("offers.shop").is(matchingShop != null ? matchingShop : "NON_EXISTENT_SHOP");
        }

        return criteria;
    }

    public List<ProductListResponse> applyPriceFiltering(List<ProductListResponse> responses) {
        return responses.stream()
                .filter(response -> {
                    if (response.getLowestCurrentPrice() == null) {
                        return false;
                    }
                    if (minPrice != null && response.getLowestCurrentPrice().compareTo(minPrice) < 0) {
                        return false;
                    }
                    return maxPrice == null || response.getLowestCurrentPrice().compareTo(maxPrice) <= 0;
                })
                .toList();
    }

    public List<ProductListResponse> applySorting(List<ProductListResponse> responses) {
        if (!pageable.getSort().isSorted()) {
            return responses;
        }

        var priceOrder = pageable.getSort().stream()
                .filter(order -> order.getProperty().equals(PRICE_FIELD))
                .findFirst();

        if (priceOrder.isEmpty()) {
            return responses;
        }

        var comparator = Comparator.comparing(
                ProductListResponse::getLowestCurrentPrice, Comparator.nullsLast(Comparator.naturalOrder()));

        if (priceOrder.get().getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        return responses.stream().sorted(comparator).toList();
    }

    public Page<ProductListResponse> applyPagination(List<ProductListResponse> responses) {
        var start = (int) pageable.getOffset();
        var end = Math.min((start + pageable.getPageSize()), responses.size());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }
}
