package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Document(collection = "products")
public class Product {
    @MongoId
    @Field("_id")
    private String id;

    @Indexed(unique = true)
    @Field("ean")
    @NonNull private String ean;

    @Indexed
    @Field("name")
    @NonNull private String name;

    @Indexed
    @Field("category")
    @NonNull private Category category;

    @Field("images")
    @NonNull private List<String> images;

    @Field("offers")
    @NonNull private List<Offer> offers;
}
