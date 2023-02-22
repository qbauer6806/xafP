package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.ProxyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Service de création d'un proxy
 *
 * @author mboutelier.ext
 */
@Component
public class ProxyServiceImpl implements ProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServiceImpl.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public Proxy createProxy() {
        String proxyUrl = gouvPropertiesResolver.getProxyUrl();
        if (StringUtils.isNotBlank(proxyUrl)) {
            String proxyPort = gouvPropertiesResolver.getProxyPort();
            LOGGER.info("CREATE PROXY WITH url={} port={}", proxyUrl, proxyPort);
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl, Integer.parseInt(proxyPort)));
        }
        LOGGER.info("NO PROXY");
        return Proxy.NO_PROXY;
    }
}
