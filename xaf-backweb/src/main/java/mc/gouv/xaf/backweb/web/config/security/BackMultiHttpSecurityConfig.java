package mc.gouv.xaf.backweb.web.config.security;

import mc.gouv.xaf.backweb.web.config.security.filter.GouvPreAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * @author mpavone
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class BackMultiHttpSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GouvPreAuthFilter gouvPreAuthFilter)
            throws Exception {

        return http.authorizeHttpRequests(
                        authz -> authz.requestMatchers("/monitor", "/css/**", "/js/**", "/font/**", "/img/**", "/webjars/**",
                                "/h2-console/**", "/fonts/**", "/dynamicjs/**").permitAll().anyRequest().authenticated())
                .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))
                .securityContext(ctx -> ctx.requireExplicitSave(false))
                .addFilterBefore(gouvPreAuthFilter, BasicAuthenticationFilter.class).build();
    }
}
