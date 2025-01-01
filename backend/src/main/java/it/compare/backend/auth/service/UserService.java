package it.compare.backend.auth.service;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.model.User;
import it.compare.backend.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createOrUpdate(OAuthUserDetails oAuthUserDetails) {
        var userId = oAuthUserDetails.getId();
        var userEntity = userRepository.findById(userId).orElse(new User());

        userEntity.setId(userId);
        userEntity.setUsername(oAuthUserDetails.getUsername());
        userEntity.setEmail(oAuthUserDetails.getEmail());

        return userRepository.save(userEntity);
    }
}
