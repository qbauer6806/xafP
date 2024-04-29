package mc.gouv.xaf.back.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyProperties {

    @Value("${mc.gouv.proxy.url:OPTIONAL}")
    private String proxyUrl;

    @Value("${mc.gouv.proxy.port:OPTIONAL}")
    private String proxyPort;

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }
}
