package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Message générique destiné au Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GUGenericKafkaMessage {

	/* 
	 * Seules informations vraiment obligatoires entre tous les messages de toutes les versions
	 * 		- La version
	 * 		- Le type
	 */
	
	protected String version;
	
	protected String type;
	
	public GUGenericKafkaMessage() {
		
	}

	public GUGenericKafkaMessage(String version) {
		this.version = version;
	}

	public GUGenericKafkaMessage(String version, String type) {
		this.version = version;
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
