package it.compare.backend.product.criteria;

import it.compare.backend.product.model.Category;
import it.compare.backend.product.model.Shop;
import it.compare.backend.product.response.ProductListResponse;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    public Query toQuery() {
        Query query = Query.query(toCriteria());

        // Add MongoDB sorting for all fields except lowestCurrentPrice
        if (pageable.getSort().isSorted()) {
            List<Sort.Order> orders = pageable.getSort().stream()
                    .filter(order -> !order.getProperty().equals("lowestCurrentPrice"))
                    .map(order -> new Sort.Order(
                            order.getDirection(), order.getProperty().toLowerCase()))
                    .toList();

            if (!orders.isEmpty()) {
                query.with(Sort.by(orders));
            }
        }

        return query;
    }

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
    /**
     * Applies price range filtering to the response list.
     * This is done in memory because lowestCurrentPrice is calculated from price history
     * and not stored directly in the database.
     */
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
    /**
     * Applies sorting by lowestCurrentPrice if requested.
     * This is done in memory because lowestCurrentPrice is a calculated field.
     * Other sorts are handled by MongoDB query directly.
     */
    public List<ProductListResponse> applySorting(List<ProductListResponse> responses) {
        if (!pageable.getSort().isSorted()) {
            return responses;
        }

        Optional<Sort.Order> priceOrder = pageable.getSort().stream()
                .filter(order -> order.getProperty().equals("lowestCurrentPrice"))
                .findFirst();

        if (priceOrder.isEmpty()) {
            return responses;
        }

        Comparator<ProductListResponse> comparator = Comparator.comparing(
                ProductListResponse::getLowestCurrentPrice, Comparator.nullsLast(Comparator.naturalOrder()));

        if (priceOrder.get().getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        return responses.stream().sorted(comparator).toList();
    }

    public Page<ProductListResponse> applyPagination(List<ProductListResponse> responses) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }
}
