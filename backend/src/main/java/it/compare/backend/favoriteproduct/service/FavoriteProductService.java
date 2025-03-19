package it.compare.backend.favoriteproduct.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.favoriteproduct.dto.FavoriteProductDto;
import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.favoriteproduct.response.FavoriteProductStatusResponse;
import it.compare.backend.product.mapper.ProductMapper;
import it.compare.backend.product.model.Product;
import it.compare.backend.product.response.ProductListResponse;
import it.compare.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FavoriteProductService {

    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final ProductService productService;
    private final FavoriteProductRepository favoriteProductRepository;

    public Page<ProductListResponse> findAllByUser(OAuthUserDetails oAuthUserDetails, Pageable pageable) {
        var userId = oAuthUserDetails.getId();

        var criteria = Criteria.where("user.$id").is(userId);
        var total = mongoTemplate.count(Query.query(criteria), FavoriteProduct.class);

        var match = Aggregation.match(criteria);

        final var PRODUCT_FIELD = "product";
        var lookupProduct = Aggregation.lookup("products", "product.$id", "_id", PRODUCT_FIELD);
        var unwindProduct = Aggregation.unwind(PRODUCT_FIELD);

        var sortByCreatedAtDesc = Aggregation.sort(Direction.DESC, "createdAt");
        var skip = Aggregation.skip(pageable.getOffset());
        var limit = Aggregation.limit(pageable.getPageSize());

        var replaceRootWithProduct = Aggregation.replaceRoot(PRODUCT_FIELD);
        var aggregation = Aggregation.newAggregation(
                FavoriteProduct.class,
                match,
                lookupProduct,
                unwindProduct,
                sortByCreatedAtDesc,
                skip,
                limit,
                replaceRootWithProduct);

        var products = mongoTemplate.aggregate(aggregation, Product.class).getMappedResults();
        var productsResponse =
                products.stream().map(productMapper::toListResponse).toList();

        return new PageImpl<>(productsResponse, pageable, total);
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
