package mc.gouv.xaf.back.paiement;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.Proxy;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, SecurityAutoConfiguration.class, KafkaAutoConfiguration.class})
@ComponentScan(basePackages = {"mc.gouv.xaf.back.service.utils", "mc.gouv.xaf.back.paiement"})
@EnableJpaRepositories(basePackages = {"mc.gouv.xaf.back.data", "mc.gouv.xaf.back.paiement.data"})
@EntityScan(basePackages = {"mc.gouv.xaf.back.data", "mc.gouv.xaf.back.paiement.data"})
@PropertySource("classpath:application-test.properties")
// TODO Corriger les tests après merge XAF 11
public class SpringTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringTestApplication.class, args);
    }

}
