package mc.gouv.xaf.back.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class ProxyProperties {

    @Value("${mc.gouv.proxy.url:OPTIONAL}")
    private String proxyUrl;

    @Value("${mc.gouv.proxy.port:OPTIONAL}")
    private String proxyPort;

}
