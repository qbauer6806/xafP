package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.GUGenericKafkaMessage;

/**
 * 
 * Message destiné au Guichet Unique via Kafka
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GUKafkaMessage extends GUGenericKafkaMessage {
	
	/**
	 * Informations de second niveau, pouvant dépendre d'une version de message : pour le moment aucune
	 * (avant "type" était ici, avant d'être remonté plus haut à côté de "version").
	 */

	public GUKafkaMessage() {
		super("v1");
	}
	
	public GUKafkaMessage(String type) {
		super("v1", type);
	}
	
}
