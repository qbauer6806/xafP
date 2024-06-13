package mc.gouv.xaf.apiclient2tiers.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.apiclient2tiers.authentication.AuthorizationHeaderProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * 
 * @author qdeme
 *
 */
public abstract class ApiClient {

    private String serviceUrl;
    private AuthorizationHeaderProvider authorizationHeaderProvider;

    /**
     * Target Jax-rs
     */
    private WebTarget target;

    private Client client;

    /**
     * Configuration par défaut de jackson avec DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false
     *
     * @param isMultipartSupported
     *            indique si le client gère du multipart
     */
    public ApiClient(String serviceUrl, AuthorizationHeaderProvider authorizationHeaderProvider,
            boolean isMultipartSupported) {
        this.serviceUrl = serviceUrl;
        this.authorizationHeaderProvider = authorizationHeaderProvider;

        final var jacksonJsonProvider = createAndConfigureJacksonJsonProvider();
        this.client = ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider));

        // Si nous faisons du multi-part
        if (isMultipartSupported) {
            client.register(MultiPartWriter.class);
        }

        this.target = client.target(serviceUrl);
    }

    private JacksonJsonProvider createAndConfigureJacksonJsonProvider() {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider();

        var om = new ObjectMapper();
        // Par défaut, on ne lève pas d'exception si des champs sont retournés dans le JSON mais n'existent pas dans le
        // DTO
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Ajout de cette configuration par défaut our ne pas avoir d'exception si des entités ont des
        // annotations @JsonFilter sans configuration de filter associé
        var filters = new SimpleFilterProvider();
        filters.setFailOnUnknownId(false);
        om.setFilterProvider(filters);
        jacksonJsonProvider.setMapper(om);

        return jacksonJsonProvider;
    }

    public ApiClient(String serviceUrl, AuthorizationHeaderProvider authorizationHeaderProvider) {
        this.serviceUrl = serviceUrl;
        this.authorizationHeaderProvider = authorizationHeaderProvider;
        final var jacksonJsonProvider = createAndConfigureJacksonJsonProvider();
        this.client = ClientBuilder.newClient(new ClientConfig(jacksonJsonProvider));
        this.target = client.target(serviceUrl);
    }

    /**
     * Constructeur permettant de configurer l'ApiCLient à partir d'un client créé et configuré en amont
     */
    public ApiClient(String serviceUrl, AuthorizationHeaderProvider authorizationHeaderProvider, Client client) {
        this.serviceUrl = serviceUrl;
        this.authorizationHeaderProvider = authorizationHeaderProvider;
        this.client = client;
        this.target = client.target(serviceUrl);
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public AuthorizationHeaderProvider getAuthorizationHeaderProvider() {
        return authorizationHeaderProvider;
    }

    public void setAuthorizationHeaderProvider(AuthorizationHeaderProvider authorizationHeaderProvider) {
        this.authorizationHeaderProvider = authorizationHeaderProvider;
    }

    public WebTarget getTarget() {
        return target;
    }

    public Client getClient() {
        return client;
    }

}
