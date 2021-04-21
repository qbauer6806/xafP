package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Sens : TS -> GU
 * Message du TS concernant la désinscription d'un usager du TS (et non du Guichet Unique)
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DesinscriptionUsagerTSMessage extends GUKafkaMessage {
	
	private String usagerId;
	
	public DesinscriptionUsagerTSMessage() {
		super("suppression-acces-ts");
	}
	
	public DesinscriptionUsagerTSMessage(String usagerId) {
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
