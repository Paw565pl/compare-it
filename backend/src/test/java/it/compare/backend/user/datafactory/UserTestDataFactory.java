package it.compare.backend.user.datafactory;

import it.compare.backend.auth.model.User;
import it.compare.backend.auth.repository.UserRepository;
import it.compare.backend.core.config.FakerConfig;
import it.compare.backend.core.datafactory.TestDataFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import net.datafaker.Faker;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;

@TestComponent
@Import(FakerConfig.class)
public class UserTestDataFactory implements TestDataFactory<User> {

    private final Faker faker;
    private final UserRepository userRepository;

    public UserTestDataFactory(Faker faker, UserRepository userRepository) {
        this.faker = faker;
        this.userRepository = userRepository;
    }

    @Override
    public User generate() {
        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(faker.internet().emailAddress());
        user.setUsername(String.valueOf(faker.name()));
        return user;
    }

    @Override
    public User createOne() {
        return userRepository.save(generate());
    }

    @Override
    public Collection<User> createMany(int count) {
        var users = new ArrayList<User>();
        for (int i = 0; i < count; i++) {
            users.add(createOne());
        }
        return users;
    }

    @Override
    public void clear() {
        userRepository.deleteAll();
    }
}
