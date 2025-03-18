package it.compare.backend.auth.model;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@ToString
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "users")
public class User {

    @MongoId
    @Field(value = "_id", targetType = FieldType.OBJECT_ID)
    @NonNull private String id;

    @Field("username")
    @NonNull private String username;

    @Field("email")
    @NonNull private String email;
}
