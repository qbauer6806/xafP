package mc.gouv.xaf.back.stc.client.monetico;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class MoneticoConfiguration {

    public final static String MONETICOPAIEMENT_VERSION = "3.0";

    public static String serviceUrl() {
        return "https://payment-api.e-i.com/test/capture_paiement.cgi";
    }

    public static String tpe() {
        return "7527409";
    }


    public static String key() {
        return "93F6C0FEFC7D13EB7AC59ECB23D942AEBAD22B91";
    }

    public static String companyCode() {
        return "PERMC";
    }


    public static String currency() {
        return "EUR";
    }

    //@Bean
    public Proxy proxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-infra", 3129));
    }

    @Bean
    @Qualifier("clientMonetico")
    public Client clientMonetico(/*Proxy proxy*/) {
        ClientConfig configuration = new ClientConfig();

        HttpUrlConnectorProvider cp = new HttpUrlConnectorProvider();
        configuration.connectorProvider(cp);
       // cp.connectionFactory(url -> (HttpURLConnection) url.openConnection(proxy));


        return ClientBuilder.newClient();
    }

}
