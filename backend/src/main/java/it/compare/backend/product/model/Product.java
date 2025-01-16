package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import it.compare.backend.comment.model.Comment;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
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
    @NonNull private List<String> images = new ArrayList<>();

    @Field("offers")
    @NonNull private List<Offer> offers = new ArrayList<>();

    @DBRef(lazy = true)
    @Field("comments")
    private List<Comment> comments = new ArrayList<>();
}
