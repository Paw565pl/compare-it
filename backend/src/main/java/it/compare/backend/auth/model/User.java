package it.compare.backend.auth.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.lang.NonNull;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Document(collection = "users")
public class User {

    @NonNull @MongoId
    @Field(name = "_id")
    private String id;

    @NonNull @Field("username")
    private String username;

    @NonNull @Field("email")
    private String email;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
