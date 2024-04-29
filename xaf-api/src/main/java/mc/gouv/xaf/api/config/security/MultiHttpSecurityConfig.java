package mc.gouv.xaf.api.config.security;

import mc.gouv.xaf.api.config.filter.jwt.JwtAuthFilter;
import mc.gouv.xaf.api.config.filter.jwt.JwtAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class MultiHttpSecurityConfig {

    @Configuration
    public static class GouvBasicSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final Logger LOGGER = LoggerFactory.getLogger(GouvBasicSecurityConfig.class);

        @Value("${application.name}")
        private String applicationName;

        @Value("${mc.gouv.${application.name}.apiserver.security.jwt.secret}")
        private String secretValue;

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authentication) throws Exception {

            var jwtAuthenticationProvider = new JwtAuthenticationProvider(applicationName, secretValue);
            authentication.authenticationProvider(jwtAuthenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            HttpSecurity httpSecured = http.antMatcher("/api/**");

            httpSecured.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeRequests().anyRequest().authenticated();

            httpSecured.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf()
                    .disable();
        }

        /**
         * Pour permettre d'accéder à la documentation /index.html
         */
        @Override
        public void configure(WebSecurity web) throws Exception {
            //https://stackoverflow.com/questions/43651298/adding-authorization-to-annotation-driven-swagger-json-with-jersey-2-and-spring/
            web.ignoring().antMatchers("/*", "/**/swagger.json", "/swagger/*", "/h2-console/**");
        }
    }
}
