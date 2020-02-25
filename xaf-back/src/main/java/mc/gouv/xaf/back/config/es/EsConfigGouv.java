package mc.gouv.xaf.back.config.es;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc.gouv.xaf.back.data.es.dao")
public class EsConfigGouv {

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    private final String GOUV_PROPERTIES_CHAR_SPLITTER = ",";

    @SuppressWarnings("resource")
    @Bean
    public RestHighLevelClient client() {

        // Authentification elastisearch
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        String user = gouvPropertiesResolver.getEsUser();
        String password = gouvPropertiesResolver.getEsPassword();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

        String[] clusterHosts = gouvPropertiesResolver.getEsClusterHosts().split(GOUV_PROPERTIES_CHAR_SPLITTER);
        List<HttpHost> hosts = new ArrayList<>();
        for (String clusterHost : clusterHosts) {
            hosts.add(new HttpHost(clusterHost, gouvPropertiesResolver.getEsPort(), "http"));
        }

        RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

        // Migration Transport -> RestHighLevel https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.5/_changing_the_client_8217_s_initialization_code.html
        return new RestHighLevelClient(builder);
    }

    @Bean
    @Primary
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

}
