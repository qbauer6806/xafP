package mc.gouv.xaf.back.config.es;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.inject.Inject;

@Configuration
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc.gouv.xaf.back.data.es.dao")
public class EsConfigGouv {

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    @SuppressWarnings("resource")
    @Bean
    public RestHighLevelClient client() {
        // Migration Transport -> RestHighLevel https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.5/_changing_the_client_8217_s_initialization_code.html
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(gouvPropertiesResolver.getEsHost(), gouvPropertiesResolver.getEsPort(), "http"),
                        new HttpHost(gouvPropertiesResolver.getEsHost(), gouvPropertiesResolver.getEsPort(), "http")));
    }

    @Bean
    @Primary
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }

}
