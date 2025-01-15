package it.compare.backend.product.model;

import com.mongodb.lang.NonNull;
import it.compare.backend.comment.model.Comment;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "products")
@CompoundIndex(
        name = "one_comment_for_product_by_author_index",
        def = "{'_id': 1, 'comments.author._id': 1}",
        unique = true)
@CompoundIndex(
        name = "one_rating_for_comment_per_product_by_author_index",
        def = "{'_id': 1, 'comments.author._id': 1, 'comments.ratings._id': 1}",
        unique = true)
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

    @DocumentReference
    @Field("comments")
    private List<Comment> comments = new ArrayList<>();
}
