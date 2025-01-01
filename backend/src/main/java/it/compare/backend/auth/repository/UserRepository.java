package it.compare.backend.auth.repository;

import it.compare.backend.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {}
