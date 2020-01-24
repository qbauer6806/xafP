#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver;

import mc.gouv.Static;
import mc.gouv.xboot.config.web.FilterConfig;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 
 * @author mpavone
 *
 */
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "${groupId}", "mc.gouv.xaf.back", "mc.gouv.xaf.backweb" })
@EnableJpaRepositories(basePackages = { "mc.gouv.xaf.back.data" })
@EntityScan(basePackages = { "mc.gouv.xaf.back.data" })
public class ${artifactIdCamelCase}BackApp {

    public static void main(String[] args) {

        if (!System.getProperties().containsKey("MC_LOGDIR")) {
            System.setProperty("MC_LOGDIR", Static.getValue("LOGDIR", "/www/logs"));
        }
        if (!System.getProperties().containsKey("MC_APPNAME")) {
            System.setProperty("MC_APPNAME", "${artifactIdLower}");
        }
        SpringApplication.run(new Class[] { ${artifactIdCamelCase}BackApp.class, FilterConfig.class }, args);

    }

}
