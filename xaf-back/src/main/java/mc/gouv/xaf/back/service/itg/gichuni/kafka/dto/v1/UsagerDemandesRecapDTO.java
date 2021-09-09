package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Bloc "usagerDemandesRecap" du message SynchronisationDemandesMessage envoyé au Guichet Unique
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagerDemandesRecapDTO {

	private String usagerId;
	
	private List<DemandeRecapDTO> demandeRecaps;
	
	private RecapDemandesDTO recapDemandes;

	public String getUsagerId() {
		return usagerId;
	}

	public void setUsagerId(String usagerId) {
		this.usagerId = usagerId;
	}

	public List<DemandeRecapDTO> getDemandeRecaps() {
		return demandeRecaps;
	}

	public void setDemandeRecaps(List<DemandeRecapDTO> demandeRecaps) {
		this.demandeRecaps = demandeRecaps;
	}

	public RecapDemandesDTO getRecapDemandes() {
		return recapDemandes;
	}

	public void setRecapDemandes(RecapDemandesDTO recapDemandes) {
		this.recapDemandes = recapDemandes;
	}
	
}
