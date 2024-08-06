package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class ArchivageProperties {

    @Value("${mc.gouv.${application.name}.shared.backapi.archivage.enabled:}")
    private String archivageEnabled;

    @Value("${mc.gouv.rio.url:#{null}}")
    private String rioUrl;

    @Value("${mc.gouv.${application.name}.shared.backapi.rio.jwt:#{null}}")
    private String rioJwt;

    @Value("${mc.gouv.${application.name}.shared.backapi.rio.codeappli:#{null}}")
    private String rioCodeAppli;

    @Value("${mc.gouv.${application.name}.shared.backapi.rio.codenotice:#{null}}")
    private String rioCodeNotice;

}
