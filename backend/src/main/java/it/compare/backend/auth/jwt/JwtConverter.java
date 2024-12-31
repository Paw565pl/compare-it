package it.compare.backend.auth.jwt;

import it.compare.backend.auth.details.OAuthUserDetails;
import it.compare.backend.auth.service.UserService;
import it.compare.backend.auth.util.JwtUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    public JwtConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        var authorities = JwtUtil.getAuthorities(jwt);
        var subject = jwt.getClaimAsString(JwtClaimNames.SUB);

        userService.createOrUpdate(OAuthUserDetails.fromJwt(jwt));

        return new JwtAuthenticationToken(jwt, authorities, subject);
    }
}
