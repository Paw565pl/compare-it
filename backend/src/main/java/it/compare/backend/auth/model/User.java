package it.compare.backend.auth.model;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "users")
public class User {

    @MongoId
    @Field("_id")
    @NonNull private String id;

    @Field("username")
    @NonNull private String username;

    @Field("email")
    @NonNull private String email;
}
