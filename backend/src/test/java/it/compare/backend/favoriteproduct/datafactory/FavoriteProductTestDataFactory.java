package it.compare.backend.favoriteproduct.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import it.compare.backend.user.datafactory.UserTestDataFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import(ProductTestDataFactory.class)
public class FavoriteProductTestDataFactory implements TestDataFactory<FavoriteProduct> {

    private final FavoriteProductRepository favoriteProductRepository;
    private final ProductTestDataFactory productTestDataFactory;
    private final UserTestDataFactory userTestDataFactory;

    public FavoriteProductTestDataFactory(
            FavoriteProductRepository favoriteProductRepository,
            ProductTestDataFactory productTestDataFactory,
            UserTestDataFactory userTestDataFactory) {
        this.favoriteProductRepository = favoriteProductRepository;
        this.productTestDataFactory = productTestDataFactory;
        this.userTestDataFactory = userTestDataFactory;
    }

    @Override
    public FavoriteProduct generate() {
        var product = productTestDataFactory.createOne();
        var user = userTestDataFactory.createOne();

        return new FavoriteProduct(user, product);
    }

    @Override
    public FavoriteProduct createOne() {
        return favoriteProductRepository.save(generate());
    }

    @Override
    public List<FavoriteProduct> createMany(int count) {
        var favoriteProducts = new ArrayList<FavoriteProduct>();
        for (int i = 0; i < count; i++) {
            favoriteProducts.add(generate());
        }

        return favoriteProductRepository.saveAll(favoriteProducts);
    }

    public List<FavoriteProduct> createMany(int count, User user) {
        var favoriteProducts = new ArrayList<FavoriteProduct>();
        for (int i = 0; i < count; i++) {
            var favoriteProduct = generate();
            favoriteProduct.setUser(user);

            favoriteProducts.add(favoriteProduct);
        }

        return favoriteProductRepository.saveAll(favoriteProducts);
    }

    @Override
    public void clear() {
        productTestDataFactory.clear();
        userTestDataFactory.clear();
        favoriteProductRepository.deleteAll();
    }
}
