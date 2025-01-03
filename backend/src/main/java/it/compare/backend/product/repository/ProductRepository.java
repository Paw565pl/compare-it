package it.compare.backend.product.repository;

import it.compare.backend.product.model.Product;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findAllByEanIn(List<String> eanList);
}
