package mc.gouv.xaf.back.config.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.NodeSelector;
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
	 * 
	 * @return
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
				.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(connectTimeout)
						.setSocketTimeout(socketTimeout));
		
		// Node selector
		
		builder.setNodeSelector(new NodeSelector() { 
		    @Override
		    public void select(Iterable<Node> nodes) {
		        /*
		         * Prefer any node that belongs to rack_one. If none is around
		         * we will go to another rack till it's time to try and revive
		         * some of the nodes that belong to rack_one.
		         */
		        boolean foundOne = false;
		        for (Node node : nodes) {
		            String rackId = node.getAttributes().get("rack_id").get(0);
		            if ("rack_one".equals(rackId)) {
		                foundOne = true;
		                break;
		            }
		        }
		        if (foundOne) {
		            Iterator<Node> nodesIt = nodes.iterator();
		            while (nodesIt.hasNext()) {
		                Node node = nodesIt.next();
		                String rackId = node.getAttributes().get("rack_id").get(0);
		                if ("rack_one".equals(rackId) == false) {
		                    nodesIt.remove();
		                }
		            }
		        }
		    }
		});

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

		try (RestHighLevelClient client = new RestHighLevelClient(builder)) {


			
			
			return client;
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	@Bean
	@Primary
	public ElasticsearchRestTemplate elasticsearchTemplate() {
		return new ElasticsearchRestTemplate(client());
	}

}
