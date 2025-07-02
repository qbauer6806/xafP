package mc.gouv.xaf.api.config.security;

import mc.gouv.xaf.api.config.filter.jwt.JwtAuthFilter;
import mc.gouv.xaf.api.config.filter.jwt.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
public class MultiHttpSecurityConfig {

    @Value("${application.name}")
    private String applicationName;

    @Value("${mc.gouv.${application.name}.apiserver.security.jwt.secret}")
    private String secretValue;

    @Bean
    public JwtAuthenticationProvider configureGlobal() {
        return new JwtAuthenticationProvider(applicationName, secretValue);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**");

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
                .requestMatchers("/*", "/swagger.json", "/swagger/*", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/paiement").permitAll()
                .anyRequest().authenticated();

        http.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf().disable();
        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http.securityMatcher("/api/v1/paiement/tarif", "/api/v1/paiement/debit", "/api/v1/paiement/rattrapageDebits", "/api/v1/paiement/recuPaiement")
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/v1/paiement/tarif", "/api/v1/paiement/debit", "/api/v1/paiement/rattrapageDebits", "/api/v1/paiement/recuPaiement").authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new CustomGrantedAuthoritiesConverter());
        return converter;
    }
}
