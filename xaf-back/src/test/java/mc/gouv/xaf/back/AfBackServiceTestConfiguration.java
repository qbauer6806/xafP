package mc.gouv.xaf.back;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@SpringBootApplication(
        exclude = { MongoAutoConfiguration.class, SecurityAutoConfiguration.class },
        scanBasePackages = { "mc.gouv.xaf.back" }
)
@EnableJpaRepositories(basePackages = { "mc.gouv.xaf.back.data" })
@EntityScan(basePackages = { "mc.gouv.xaf.back.data" })
@PropertySource("classpath:application-test.properties")
public class AfBackServiceTestConfiguration {
}
