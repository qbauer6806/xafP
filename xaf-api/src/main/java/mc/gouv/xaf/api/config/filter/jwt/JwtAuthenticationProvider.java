package mc.gouv.xaf.api.config.filter.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Authentification via la vérification du token JWT
 * Le principal sera lié à la valeur du payload "sub"
 *
 * @author fgaujous
 */
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationProvider.class);
    //Header définissant l'algo de signature utilisé
    private static final String JWT_HEADER_ALG = "alg";

    /**
     * Roles de l'utilisateur
     */
    private static final String JWT_PAYLOAD_ROLES = "roles";

    private static final String JWT_PAYLOAD_GOUV = "gouv";

    private static final String JWT_PAYLOAD_SHARED = "shared";

    private String applicationName;

    private String secretValue;

    public JwtAuthenticationProvider(String applicationName, String secretValue) {
        this.applicationName = applicationName;
        this.secretValue = secretValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Jws<Claims> jws = null;
        List<String> roles = null;
        JwtAuthToken authenticationJwt = (JwtAuthToken) authentication;
        try {
            jws = verify(authenticationJwt.getToken());
            Object algoObj = jws.getHeader().get(JWT_HEADER_ALG);
            if (algoObj == null) {
                LOGGER.error("Aucun algorithme mentionné");
                throw new BadCredentialsException("Aucun algorithme mentionné pour la signature du jwt");
            }
            String algo = (String) algoObj;
            if (!StringUtils.equals(algo, SignatureAlgorithm.HS256.name())) {
                LOGGER.error("algorithme utilisé : {} != HS256", algo);
                throw new BadCredentialsException("Erreur dans l'algorithme utilisé pour la signature du jwt");
            }

            Map<?, ?> mapGouvProperties = (Map<?, ?>) jws.getBody().get(JWT_PAYLOAD_GOUV);
            if (mapGouvProperties == null) {
                LOGGER.error("gouv manquant dans le token JWT");
                throw new BadCredentialsException("Element gouv manquant dans le JWT");
            }
            Map<?, ?> mapSharedProperties = (Map<?, ?>) mapGouvProperties.get(JWT_PAYLOAD_SHARED);
            if (mapSharedProperties == null) {
                LOGGER.error("gouv.shared manquant dans le token JWT");
                throw new BadCredentialsException("gouv.shared gouv manquant dans le JWT");
            }
            roles = (ArrayList<String>) mapSharedProperties.get(JWT_PAYLOAD_ROLES);

            if (roles == null || roles.isEmpty()) {
                LOGGER.error("roles manquant dans le token JWT");
                throw new BadCredentialsException("Element roles manquant dans le JWT");
            }
            String aud = null;
            Set<String> audiences = jws.getPayload().getAudience();
            Optional<String> optional = audiences.stream().findFirst();
            if (optional.isPresent()) {
                 aud = optional.get();
            }
            if (StringUtils.isBlank(aud)) {
                LOGGER.error("aud manquant dans le token JWT");
                throw new BadCredentialsException("Element aud manquant dans le JWT");
            }
            if (!StringUtils.equalsIgnoreCase(applicationName, aud)) {
                LOGGER.error("Element aud invalide");
                throw new BadCredentialsException("Element aud invalide");
            }

        } catch (Exception e) {
            LOGGER.error("Erreur lors de la vérification du token JWT", e);
            throw new BadCredentialsException("Erreur lors de la vérification du token JWT");
        }
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        String subject = jws.getBody().getSubject();
        if (StringUtils.isBlank(subject)) {
            LOGGER.error("sub manquant dans le token JWT");
            throw new BadCredentialsException("Element sub manquant dans le JWT");
        }

        LOGGER.debug("Ajout des roles {} pour l'utilisateur {}", roles, subject);
        for (String role : roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        Authentication authFinal = new JwtAuthToken(subject, jws, grantedAuthorities, applicationName);
        SecurityContextHolder.getContext().setAuthentication(authFinal);
        return authFinal;
    }

    /**
     * utilisation de la librairie https://github.com/jwtk/jjwt
     * Vérification du token avec la signature
     *
     * @param token
     * @return
     * @throws SignatureException       si jwt invalide
     * @throws IllegalArgumentException
     * @throws MalformedJwtException
     * @throws UnsupportedJwtException
     * @throws ExpiredJwtException
     */
    public Jws<Claims> verify(String token) throws SignatureException, ExpiredJwtException, UnsupportedJwtException,
            MalformedJwtException, IllegalArgumentException {

        if (secretValue == null) {
            throw new DemarcheException("Aucune clé JWT n'a été trouvée, veuillez renseigner mc.gouv.api.<applicationName>.security.jwt.secret ou mc.gouv.<applicationName>.api.security.jwt.secret");
        }
        return Jwts.parser().setSigningKey(secretValue.getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.equals(authentication);
    }

}
