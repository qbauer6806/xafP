package mc.gouv.af.back;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "mc.gouv.af.back", "mc.gouv.dem" })
@EnableJpaRepositories(basePackages = { "mc.gouv.dem.data" })
@EntityScan(basePackages = { "mc.gouv.dem.data" })
@PropertySource("classpath:application-test.properties")
public class AfBackServiceTestConfiguration {
}
