package mc.gouv.xaf.back.config.es;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

@Configuration
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc.gouv.xaf.back.data.es.dao")
public class EsConfigGouv {

	private static final Logger LOGGER = LoggerFactory.getLogger(EsConfigGouv.class);

	@Inject
	private GouvPropertiesResolver gouvPropertiesResolver;

	private final String GOUV_PROPERTIES_CHAR_SPLITTER = ",";

	/**
	 * Méthode permettant de configurer un High Lvel REST Elasticsearch client.
	 *
	 * @return RestHighLevelClient
	 */
	@Bean
	public RestHighLevelClient client() {

		String[] clusterHosts = StringUtils.split(gouvPropertiesResolver.getEsClusterHosts(),
				GOUV_PROPERTIES_CHAR_SPLITTER);

		List<HttpHost> hosts = new ArrayList<>();
		for (String clusterHost : clusterHosts) {
			hosts.add(new HttpHost(clusterHost, gouvPropertiesResolver.getEsPort()));
		}

		Integer connectTimeout = gouvPropertiesResolver.getEsConnectTimeout();
		Integer socketTimeout = gouvPropertiesResolver.getEsSocketTimeout();
		RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[hosts.size()]))
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

	/**
	 * Méthode permettant de configurer un convertisseur elasticsearch pour remplacer le convertisseur Jackson.
	 *
	 * @return RestHighLevelClient
	 */
	@Bean
	public ElasticsearchConverter elasticsearchConverter() {
		return new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
	}

	/**
	 * Méthode permettant de configurer un template pour effectuer des actions sur le serveur elasticsearch.
	 *
	 * @return RestHighLevelClient
	 */
	@Bean
	@Primary
	public ElasticsearchRestTemplate elasticsearchTemplate() {
		return new ElasticsearchRestTemplate(client(), elasticsearchConverter());
	}

}
