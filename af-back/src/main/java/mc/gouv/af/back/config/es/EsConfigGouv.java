package mc.gouv.af.back.config.es;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import mc.gouv.af.back.properties.GouvPropertiesResolver;

@Configuration
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc.gouv.af.back.data.es.dao")
public class EsConfigGouv {

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static final String CLUSTER_NAME_PROPERTY = "cluster.name";

    @SuppressWarnings("resource")
    @Bean
    public Client client() throws UnknownHostException {
        Settings esSettings = Settings.builder().put(CLUSTER_NAME_PROPERTY, gouvPropertiesResolver.getEsClusterName())
                .build();
        TransportClient client = new PreBuiltTransportClient(esSettings);
        // https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
        return client.addTransportAddress(new InetSocketTransportAddress(
                InetAddress.getByName(gouvPropertiesResolver.getEsHost()), gouvPropertiesResolver.getEsPort()));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
        return new ElasticsearchTemplate(client());
    }

}
