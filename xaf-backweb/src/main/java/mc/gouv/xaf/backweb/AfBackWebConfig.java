package mc.gouv.xaf.backweb;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
@EnableCaching
@Profile("gouv")
public class AfBackWebConfig implements WebMvcConfigurer {

    @Bean
    public MappedInterceptor mappedInterceptor(GouvInterceptor gouvInterceptor) {
        return new MappedInterceptor(null, gouvInterceptor);
    }

}
