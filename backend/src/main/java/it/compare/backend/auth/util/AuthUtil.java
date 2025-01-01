package it.compare.backend.auth.util;

import it.compare.backend.auth.model.Role;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public abstract class AuthUtil {

    private AuthUtil() {
        throw new IllegalStateException("Attempted to instantiate utility class");
    }

    public static boolean hasRole(Collection<? extends GrantedAuthority> authorities, Role role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(role.getPrefixedRole()));
    }
}
