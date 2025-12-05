package mc.gouv.xaf.api.config.filter.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtre d'authentification JWT pour Spring Security.
 * Ce filtre intercepte toutes les requêtes HTTP entrantes et vérifie la présence
 * d'un token JWT dans l'en-tête Authorization (format "Bearer {token}").
 * Fonctionnement :
 * 1. Extrait le token JWT de l'en-tête "Authorization" de la requête
 * 2. Valide le token via le JwtAuthenticationProvider
 * 3. Si le token est valide, place l'authentification dans le SecurityContext
 * 4. Si le token est invalide ou absent, laisse Spring Security gérer l'accès non autorisé
 *
 * @author fgaujous
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public JwtAuthFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                String token = authorization.substring(7);
                JwtAuthToken jwtToken = new JwtAuthToken(token);

                Authentication validatedAuth = jwtAuthenticationProvider.authenticate(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(validatedAuth);

            } catch (AuthenticationException e) {
                LOGGER.error("Authentication failed", e);
                // Log et continuer sans authentifier
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
