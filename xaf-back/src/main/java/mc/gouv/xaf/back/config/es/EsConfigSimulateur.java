package mc.gouv.xaf.back.config.es;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

@Profile("simulateur")
@Configuration
@Conditional(IndexationEnabledCondition.class)
@EnableElasticsearchRepositories(basePackages = "mc/gouv/af/back/data/es/dao")
public class EsConfigSimulateur {

    @Inject
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    NodeBuilder nodeBuilder;

    Node node;

    @PostConstruct
    public void init() throws NodeValidationException {
        node = nodeBuilder.getNode(gouvPropertiesResolver.getEsNodeName(), gouvPropertiesResolver.getEsPort());
        node.start();
    }

    @Bean
    public Node node() {
        return node;
    }

    @Bean
    public Client client() {
        return node.client();
    }

}
