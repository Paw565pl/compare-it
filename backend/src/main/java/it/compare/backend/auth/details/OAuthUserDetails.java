package it.compare.backend.auth.details;

import it.compare.backend.auth.util.JwtUtil;
import java.util.Collection;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

@Getter
@ToString
public class OAuthUserDetails {

    private final String id;
    private final String username;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    private OAuthUserDetails(
            String id, String username, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
        this.email = email;
    }

    public static OAuthUserDetails fromJwt(Jwt jwt) {
        var id = JwtUtil.getUserId(jwt);
        var username = JwtUtil.getUsername(jwt);
        var email = JwtUtil.getEmail(jwt);
        var authorities = JwtUtil.getAuthorities(jwt);

        return new OAuthUserDetails(id, username, email, authorities);
    }
}
