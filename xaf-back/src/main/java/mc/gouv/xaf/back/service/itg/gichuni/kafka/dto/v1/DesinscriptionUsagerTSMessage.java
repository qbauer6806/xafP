package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Sens : TS -> GU (topic ts-to-gichuni)
 * 
 * Lorsqu'un usager se désinscrit d'un TS, le TS envoie un message au GU afin qu'il mette à jour sa liste de correspondance entre les usagers et les TS sur lesquels ils sont inscrits.
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DesinscriptionUsagerTSMessage extends GUKafkaMessage {
	
	private String demarcheId;
	
	private String usagerId;
	
	public DesinscriptionUsagerTSMessage() {
		super("suppression-acces-ts");
	}
	
	public DesinscriptionUsagerTSMessage(String demarcheId, String usagerId) {
		this();
		this.demarcheId = demarcheId;
		this.usagerId = usagerId;
	}

	public String getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(String usagerId) {
		this.usagerId = usagerId;
	}

	public String getDemarcheId() {
		return demarcheId;
	}

	public void setDemarcheId(String demarcheId) {
		this.demarcheId = demarcheId;
	}
	
}
