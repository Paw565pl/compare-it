package it.compare.backend.core.mock;

import it.compare.backend.auth.model.Role;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class AuthMock {

    public static Jwt getToken(String userId, String username, String email, List<Role> roles) {
        var now = Instant.now();
        return Jwt.withTokenValue("mock-token")
                .header("alg", "HS256")
                .issuer("self")
                .header("typ", "JWT")
                .claim("sub", userId)
                .claim("preferred_username", username)
                .claim("email", email)
                .claim("realm_access", Map.of("roles", roles))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
    }
}
