package it.compare.backend.favoriteproduct.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.favoriteproduct.dto.FavoriteProductDto;
import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.favoriteproduct.response.FavoriteProductStatusResponse;
import it.compare.backend.product.aggregation.ProductAggregationBuilder;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FavoriteProductService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final FavoriteProductRepository favoriteProductRepository;

    public Page<ProductListResponse> findAllByUser(OAuthUserDetails oAuthUserDetails, Pageable pageable) {
        record FavoriteProductAggregationResult(ObjectId productId) {}

        var userId = oAuthUserDetails.getId();
        var sortDirection = pageable.getSort().stream()
                .filter(sort -> sort.getProperty().equalsIgnoreCase("createdAt"))
                .findFirst()
                .map(Sort.Order::getDirection)
                .orElse(Sort.Direction.DESC);

        var match = Aggregation.match(Criteria.where("user.$id").is(userId));
        var sort = Aggregation.sort(sortDirection, "createdAt");
        AggregationOperation project =
                context -> new Document("$project", new Document("productId", "$product.$id").append("_id", 0));

        var favoriteProductAggregation = Aggregation.newAggregation(FavoriteProduct.class, match, sort, project);
        var favoriteProductIds = mongoTemplate
                .aggregate(favoriteProductAggregation, FavoriteProductAggregationResult.class)
                .getMappedResults()
                .stream()
                .map(FavoriteProductAggregationResult::productId)
                .toList();

        if (favoriteProductIds.isEmpty()) return Page.empty(pageable);
        var total = favoriteProductIds.size();

        var matchFavoriteProductIds = Aggregation.match(Criteria.where("_id").in(favoriteProductIds));
        var aggregationBuilder = ProductAggregationBuilder.builder().build();

        var aggregationPipeline = new ArrayList<AggregationOperation>();
        aggregationPipeline.add(matchFavoriteProductIds);
        aggregationPipeline.addAll(aggregationBuilder.createBaseAggregationOperations());
        aggregationPipeline.addAll(aggregationBuilder.createPriceFilterOperations());
        aggregationPipeline.addAll(aggregationBuilder.createGroupingOperations());

        // add temporary field to sort by
        final var FAVORITED_AT_FIELD = "__favoritedAt";
        var addFavoritedAtField = Aggregation.addFields()
                .addField(FAVORITED_AT_FIELD)
                .withValue(
                        ArrayOperators.IndexOfArray.arrayOf(favoriteProductIds).indexOf("$_id"))
                .build();
        aggregationPipeline.add(addFavoritedAtField);

        var sortByFavoritedAt = Aggregation.sort(sortDirection, FAVORITED_AT_FIELD);
        aggregationPipeline.add(sortByFavoritedAt);

        aggregationPipeline.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
        aggregationPipeline.add(Aggregation.limit(pageable.getPageSize()));
        aggregationPipeline.add(aggregationBuilder.createProjectionOperation());

        var aggregation = Aggregation.newAggregation(Product.class, aggregationPipeline);
        var productResponses =
                mongoTemplate.aggregate(aggregation, ProductListResponse.class).getMappedResults();

        return new PageImpl<>(productResponses, pageable, total);
    }

    public FavoriteProductStatusResponse findFavoriteProductStatus(
            OAuthUserDetails oAuthUserDetails, String productId) {
        productService.findProductOrThrow(productId);
        var favoriteProductStatusResponse = new FavoriteProductStatusResponse();

        var isFavorite = favoriteProductRepository.existsByUserIdAndProductId(oAuthUserDetails.getId(), productId);
        favoriteProductStatusResponse.setIsFavorite(isFavorite);

        return favoriteProductStatusResponse;
    }

    @Transactional
    public void addFavoriteProduct(OAuthUserDetails oAuthUserDetails, FavoriteProductDto favoriteProductDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(favoriteProductDto.productId());

        try {
            var newFavoriteProduct = new FavoriteProduct(user, product);
            favoriteProductRepository.save(newFavoriteProduct);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("This product is already in your favorites.");
        }
    }

    @Transactional
    public void removeFavoriteProduct(OAuthUserDetails oAuthUserDetails, FavoriteProductDto favoriteProductDto) {
        var user = userRepository
                .findById(oAuthUserDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var product = productService.findProductOrThrow(favoriteProductDto.productId());

        var favoriteProduct = favoriteProductRepository
                .findByUserIdAndProductId(user.getId(), product.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "This product is not in your favorites."));
        favoriteProductRepository.delete(favoriteProduct);
    }
}
