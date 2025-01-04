package it.compare.backend.auth.util;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

public abstract class JwtUtil {

    private JwtUtil() {
        throw new IllegalStateException("Attempted to instantiate utility class");
    }

    public static String getUserId(Jwt jwt) {
        return jwt.getClaimAsString(JwtClaimNames.SUB);
    }

    public static String getUsername(Jwt jwt) {
        return jwt.getClaimAsString("preferred_username");
    }

    public static String getEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    @SuppressWarnings({"unchecked", "java:S1452"})
    public static Collection<? extends GrantedAuthority> getAuthorities(Jwt jwt) {
        try {
            var realmAccess = jwt.getClaimAsMap("realm_access");
            var roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                    .toList();
        } catch (IllegalArgumentException | NullPointerException e) {
            return List.of();
        }
    }
}
