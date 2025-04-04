package it.compare.backend.favoriteproduct.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.core.datafactory.TestDataFactory;
import it.compare.backend.favoriteproduct.model.FavoriteProduct;
import it.compare.backend.favoriteproduct.repository.FavoriteProductRepository;
import it.compare.backend.product.datafactory.ProductTestDataFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import(ProductTestDataFactory.class)
public class FavoriteProductTestDataFactory implements TestDataFactory<FavoriteProduct> {

    private final FavoriteProductRepository favoriteProductRepository;
    private final ProductTestDataFactory productTestDataFactory;
    private final UserRepository userRepository;

    public FavoriteProductTestDataFactory(
            FavoriteProductRepository favoriteProductRepository,
            ProductTestDataFactory productTestDataFactory,
            UserRepository userRepository) {
        this.favoriteProductRepository = favoriteProductRepository;
        this.productTestDataFactory = productTestDataFactory;
        this.userRepository = userRepository;
    }

    @Override
    public FavoriteProduct generate() {
        var product = productTestDataFactory.createOne();

        // TODO: change this to use user data factory
        var user = new User("1", "test", "test@test.com");
        userRepository.save(user);

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

    @Override
    public void clear() {
        productTestDataFactory.clear();
        userRepository.deleteAll();
        favoriteProductRepository.deleteAll();
    }
}
