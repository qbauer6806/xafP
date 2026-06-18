package mc.gouv.xaf.back.service.itg.mail;

import mc.gouv.xaf.apiclient.mail.MailClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

@Configuration
public class MailClientConfiguration {

    @Bean
    public MailClient mailClient(GouvPropertiesResolver gouvPropertiesResolver) {
        return new MailClient(
                gouvPropertiesResolver.getMailUrl(),
                gouvPropertiesResolver.getMailJwt()
        );
    }
}
