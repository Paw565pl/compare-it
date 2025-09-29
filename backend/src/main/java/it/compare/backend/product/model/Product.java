package it.compare.backend.product.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "products")
public class Product {
    @MongoId
    @Field(name = "_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @NonNull @Indexed(unique = true)
    @Field("ean")
    private String ean;

    @NonNull @TextIndexed
    @Field("name")
    private String name;

    @NonNull @Indexed
    @Field("category")
    private Category category;

    @NonNull @Field("images")
    private List<String> images = new ArrayList<>();

    @NonNull @Field("offers")
    private List<Offer> offers = new ArrayList<>();

    @NonNull @Field("computedState")
    private ComputedState computedState = new ComputedState();

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
