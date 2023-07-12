package mc.gouv.xaf.back.config.es;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe de configuration pour ElasticSearch, à étendre dans les démarches pour configurer le configurateur ES afin de mapper les enums.
 * <p>
 * La configuration du template ES (pour faire toutes les actions) est faite dans {@link org.springframework.data.elasticsearch.config.ElasticsearchConfigurationSupport}
 */
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc.gouv.xaf.back.data.es.dao")
public class EsConfigGouv extends AbstractElasticsearchConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsConfigGouv.class);

    private static final String GOUV_PROPERTIES_CHAR_SPLITTER = ",";

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * Méthode permettant de configurer un High Lvel REST Elasticsearch client.
     * @deprecated remplacer l'utilisation du RestHighLevelClient par Elasticsearch Java API Client
     * @return RestHighLevelClient
     */
    @NotNull
    @Bean
    @Override
    @Deprecated(forRemoval = true)
    public RestHighLevelClient elasticsearchClient() {

        String[] clusterHosts = StringUtils.split(gouvPropertiesResolver.getEsClusterHosts(),
                GOUV_PROPERTIES_CHAR_SPLITTER);

        List<HttpHost> hosts = new ArrayList<>();
        for (String clusterHost : clusterHosts) {
            hosts.add(new HttpHost(clusterHost, gouvPropertiesResolver.getEsPort()));
        }

        Integer connectTimeout = gouvPropertiesResolver.getEsConnectTimeout();
        Integer socketTimeout = gouvPropertiesResolver.getEsSocketTimeout();
        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout));

        // Authentification elastisearch
        String user = gouvPropertiesResolver.getEsUser();
        String password = gouvPropertiesResolver.getEsPassword();

        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password)) {
            LOGGER.info("Configuration d'elasticsearch avec Basic Auth");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            builder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }
        return new RestHighLevelClient(builder);
    }

}
