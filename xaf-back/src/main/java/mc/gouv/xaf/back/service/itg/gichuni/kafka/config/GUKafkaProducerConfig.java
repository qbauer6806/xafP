package mc.gouv.xaf.back.service.itg.gichuni.kafka.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

import mc.gouv.xaf.back.config.KafkaEnabledCondition;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.impl.GUKafkaProducerListener;

/**
 * 
 * Configuration du Producer Kafka pour le Guichet Unique
 * 
 * @author qdeme
 *
 */
@EnableKafka
@EnableAsync
@Configuration
@Conditional(KafkaEnabledCondition.class)
public class GUKafkaProducerConfig {
	
	@Autowired
	private GUKafkaProducerListener guKafkaProducerListener;
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
          gouvPropertiesResolver.getGUKafkaBootstrapServersConfig());
        configProps.put(
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        configProps.put(
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        
        boolean sslEnabled = gouvPropertiesResolver.getGUKafkaSSLEnabled();
        if (sslEnabled) {
        	
        	String hostname = "";
        	try {
				InetAddress ip = InetAddress.getLocalHost();
				hostname = ip.getHostName();	
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	configProps.put("security.protocol", "SSL");
        	
        	configProps.put("ssl.truststore.location", gouvPropertiesResolver.getGUKafkaSSLTrustStoreLocation());
        	
        	Map<String,String> map = getHostnamePasswordMap(gouvPropertiesResolver.getGUKafkaSSLTrustStorePassword());
        	System.out.println("map1=" + map);
        	System.out.println("mdp1=" + map.get(hostname));
        	configProps.put("ssl.truststore.password", map.get(hostname));

        	map = getHostnamePasswordMap(gouvPropertiesResolver.getGUKafkaSSLKeyStorePassword());
        	System.out.println("map2=" + map);
        	System.out.println("mdp2=" + map.get(hostname));
        	configProps.put("ssl.key.password", map.get(hostname));
        	configProps.put("ssl.keystore.password", map.get(hostname));
        	configProps.put("ssl.keystore.location", gouvPropertiesResolver.getGUKafkaSSLKeyStoreLocation());
        }
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    private Map<String,String> getHostnamePasswordMap(String prop) {
    	Map<String,String> map = new HashMap<String,String>();
    	String[] hosts = prop.split(",");
    	for (String host : hosts) {
    		String[] tokens = host.split("!");
    		map.put(tokens[0], tokens[1]);
    	}
    	return map;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
    	KafkaTemplate<String, String> kt = new KafkaTemplate<>(producerFactory());
    	kt.setProducerListener(guKafkaProducerListener);
        return kt;
    }
}
