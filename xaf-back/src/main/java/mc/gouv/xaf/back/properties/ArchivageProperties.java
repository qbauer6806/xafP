package mc.gouv.xaf.back.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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

    public String getArchivageEnabled() {
        return archivageEnabled;
    }

    public void setArchivageEnabled(String archivageEnabled) {
        this.archivageEnabled = archivageEnabled;
    }

    public String getRioUrl() {
        return rioUrl;
    }

    public String getRioJwt() {
        return rioJwt;
    }

    public void setRioJwt(String rioJwt) {
        this.rioJwt = rioJwt;
    }

    public void setRioUrl(String rioUrl) {
        this.rioUrl = rioUrl;
    }

    public String getRioCodeAppli() {
        return rioCodeAppli;
    }

    public void setRioCodeAppli(String rioCodeAppli) {
        this.rioCodeAppli = rioCodeAppli;
    }

    public String getRioCodeNotice() {
        return rioCodeNotice;
    }

    public void setRioCodeNotice(String rioCodeNotice) {
        this.rioCodeNotice = rioCodeNotice;
    }
}
