package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "products")
public class Product {
    @MongoId
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Indexed(unique = true)
    @Field("ean")
    @NonNull private String ean;

    @TextIndexed
    @Field("name")
    @NonNull private String name;

    @Indexed
    @Field("category")
    @NonNull private Category category;

    @Field("images")
    @NonNull private List<String> images = new ArrayList<>();

    @Field("offers")
    @NonNull private List<Offer> offers = new ArrayList<>();
}
