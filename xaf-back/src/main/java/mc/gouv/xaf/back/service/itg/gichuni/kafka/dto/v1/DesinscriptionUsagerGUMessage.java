package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Sens : GU -> TS
 * Message du Guichet Unique concernant la désinscription d'un usager du Guichet Unique (et non du TS)
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DesinscriptionUsagerGUMessage extends GUKafkaMessage {
	
	private String usagerId;
	
	public DesinscriptionUsagerGUMessage() {
		super("desinscription-usager-gu");
	}
	
	public DesinscriptionUsagerGUMessage(String usagerId) {
		this();
		this.usagerId = usagerId;
	}

	public String getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(String usagerId) {
		this.usagerId = usagerId;
	}
	
}
