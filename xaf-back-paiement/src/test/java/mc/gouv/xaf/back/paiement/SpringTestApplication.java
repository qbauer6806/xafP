package mc.gouv.xaf.back.paiement;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.Proxy;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, SecurityAutoConfiguration.class})
@EnableJpaRepositories("mc.gouv.xaf.back")
@EntityScan("mc.gouv.xaf.back")
@PropertySource("classpath:application-test.properties")
public class SpringTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

    @Bean
    Proxy proxy() {
        return Proxy.NO_PROXY;
    }
}
