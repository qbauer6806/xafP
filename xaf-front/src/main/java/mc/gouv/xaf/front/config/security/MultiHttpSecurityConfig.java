package mc.gouv.xaf.front.config.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 
 * Sécurisation par JWT des endpoints /api2tiers/**
 * 
 * @author qdeme
 *
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class MultiHttpSecurityConfig {

    @Configuration
    public static class GouvBasicSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final Logger LOGGER = LoggerFactory.getLogger(GouvBasicSecurityConfig.class);
        
        public static final String LANCEMENT_IMPOSSIBLE_MSG = "Lancement impossible sans la propriété ";

        @Value("${application.name}")
        String applicationName;

        @Autowired
        private Environment env;

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder authentication) throws Exception {

            String secretProp = "mc.gouv.api." + applicationName + ".security.jwt.secret";
            LOGGER.info("Vérification de la présence d'une valeur pour la propriété : {}", secretProp);
            String secretKey = env.getProperty(secretProp);
            if (StringUtils.isBlank(secretKey)) {
                secretProp = "mc.gouv." + applicationName + ".front.2tiers.security.jwt.secret";
                secretKey = env.getProperty(secretProp);
                if (StringUtils.isBlank(secretKey)) {
                    throw new Exception(LANCEMENT_IMPOSSIBLE_MSG + secretProp);
                }
            }

            var jwtAuthenticationProvider = new JwtAuthenticationProvider();
            jwtAuthenticationProvider.setApplicationName(applicationName);
            jwtAuthenticationProvider.setEnvironment(env);
            authentication.authenticationProvider(jwtAuthenticationProvider);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            HttpSecurity httpSecured = http.antMatcher("/api2tiers/**");

            httpSecured.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                    .authorizeRequests().anyRequest().authenticated();

            httpSecured.addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class).csrf()
                    .disable();
        }

        /**
         * Pour permettre d'accéder au reste de l'appli
         */
        @Override
        public void configure(WebSecurity web) throws Exception {
            //https://stackoverflow.com/questions/43651298/adding-authorization-to-annotation-driven-swagger-json-with-jersey-2-and-spring/
            web.ignoring().antMatchers("/*");
        }
    }
}
