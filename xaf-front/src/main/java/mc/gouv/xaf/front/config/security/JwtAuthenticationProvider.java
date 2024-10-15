package mc.gouv.xaf.front.config.security;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Authentification via la vérification du token JWT
 * Le principal sera lié à la valeur du payload "sub"
 * @author fgaujous, qdeme
 *
 */
@Setter
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationProvider.class);
    
    public static final String MC_GOUV = "mc.gouv.";
    
    //Header définissant l'algo de signature utilisé
    private static final String JWT_HEADER_ALG = "alg";

    /**
     * https://tools.ietf.org/html/rfc7519#section-4.1.2
     */

    private static final String JWT_PAYLOAD_AUD = "aud";

    /**
     * Roles de l'utilisateur
     */
    private static final String JWT_PAYLOAD_ROLES = "roles";

    private static final String JWT_PAYLOAD_GOUV = "gouv";

    private static final String JWT_PAYLOAD_SHARED = "shared";

    @Value("${application.name}")
    String applicationName;

    private Environment environment;

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

            String aud = (String) jws.getBody().get(JWT_PAYLOAD_AUD);
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
     * @param token
     * @return
     */
    public Jws<Claims> verify(String token) {

        String secretProp = MC_GOUV + applicationName + ".frontserver.2tiers.security.jwt.secret";
        String secretKey = environment.getProperty(secretProp);
        if (StringUtils.isBlank(secretKey)) {
            secretProp = MC_GOUV + applicationName + ".frontserver.2tiers.security.jwt.secret";
            secretKey = environment.getProperty(secretProp);
        }
        if (secretKey == null) {
            throw new DemarcheException("Aucune clé JWT n'a été trouvée, veuillez renseigner mc.gouv.api.<applicationName>.security.jwt.secret ou mc.gouv.<applicationName>.api.security.jwt.secret");
        }
        return Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).build().parseClaimsJws(token);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.equals(authentication);
    }

}
