package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Sens : GU -> TS (topic gichuni-to-ts-*)
 *
 * Lorsqu'un usager se désinscrit du GU, le GU doit envoyer un message à chacun des TS sur lesquels cet usager était inscrit.
 * Il faut donc envoyer un message sur chacun des topics correspondants : gichuni-to-ts-{tsCode1}, gichuni--to-ts-{tsCode2}, etc.
 * {tsCode} étant le code appli du TS en minuscule, par exemple : insspin, subveco, stage...
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DesinscriptionUsagerGUMessage extends GUKafkaMessage {
	
	private String usagerId;
	
	public DesinscriptionUsagerGUMessage() {
		super("desinscription-usager-gichuni");
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
