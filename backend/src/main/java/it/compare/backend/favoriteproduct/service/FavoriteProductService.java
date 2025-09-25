package it.compare.backend.favoriteproduct.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.favoriteproduct.dto.FavoriteProductRequestDto;
import it.compare.backend.favoriteproduct.dto.FavoriteProductStatusResponseDto;
import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.product.dto.ProductListResponseDto;
import it.compare.backend.product.service.ProductService;
import java.util.ArrayList;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FavoriteProductService {

    private static final String PRODUCT_FIELD = "product";

    private final UserRepository userRepository;
    private final FavoriteProductRepository favoriteProductRepository;
    private final ProductService productService;
    private final MongoTemplate mongoTemplate;

    public Page<ProductListResponseDto> findAllByUser(OAuthUserDetails oAuthUserDetails, Pageable pageable) {
        var criteria = Criteria.where("user.$id").is(oAuthUserDetails.getId());
        var total = mongoTemplate.count(Query.query(criteria), FavoriteProduct.class);
        if (total == 0) return Page.empty(pageable);

        var operations = new ArrayList<AggregationOperation>();
        operations.add(Aggregation.match(criteria));

        operations.add(Aggregation.lookup("products", "product.$id", "_id", PRODUCT_FIELD));
        operations.add(Aggregation.unwind(PRODUCT_FIELD));

        var sortOrders = Stream.concat(
                        pageable.getSort().stream().filter(o -> o.getProperty().equalsIgnoreCase("createdAt")),
                        productService.getListSortOrders(pageable).stream()
                                .map(o -> new Sort.Order(o.getDirection(), PRODUCT_FIELD + "." + o.getProperty())))
                .toList();

        operations.add(Aggregation.sort(Sort.by(sortOrders)));
        operations.add(Aggregation.replaceRoot(PRODUCT_FIELD));

        operations.add(Aggregation.skip(pageable.getOffset()));
        operations.add(Aggregation.limit(pageable.getPageSize()));

        var aggregation = Aggregation.newAggregation(FavoriteProduct.class, operations);
        var content = mongoTemplate
                .aggregate(aggregation, ProductListResponseDto.class)
                .getMappedResults();

        return new PageImpl<>(content, pageable, total);
    }

    public FavoriteProductStatusResponseDto findStatus(OAuthUserDetails oAuthUserDetails, String productId) {
        productService.findProductOrThrow(productId);
        var isFavorite = favoriteProductRepository.existsByUserIdAndProductId(oAuthUserDetails.getId(), productId);

        return new FavoriteProductStatusResponseDto(isFavorite);
    }

    @Transactional
    public void add(OAuthUserDetails oAuthUserDetails, FavoriteProductRequestDto favoriteProductRequestDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(favoriteProductRequestDto.productId());

        try {
            var newFavoriteProduct = new FavoriteProduct(user, product);
            favoriteProductRepository.save(newFavoriteProduct);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("This product is already in your favorites");
        }
    }

    @Transactional
    public void remove(OAuthUserDetails oAuthUserDetails, FavoriteProductRequestDto favoriteProductRequestDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(favoriteProductRequestDto.productId());

        var favoriteProduct = favoriteProductRepository
                .findByUserIdAndProductId(user.getId(), product.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "This product is not in your favorites"));
        favoriteProductRepository.delete(favoriteProduct);
    }
}
