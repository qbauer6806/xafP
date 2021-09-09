package mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Bloc "recapDemandes" du message SynchronisationDemandesMessage envoyé au Guichet Unique
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecapDemandesDTO {
	
	/**
	 * Nombre total de demandes effectuées par l'usager, quelque soit leur statut
	 */
	private Integer total;
	
	/**
	 * Nombre de demandes en cours de l'usager (moins celles en attente de l'usager)
	 */
	private Integer enCours;
	
	/**
	 * Nombre de demandes de l'usager qui sont en attente d'une action de sa part
	 */
	private Integer enAttenteUsager;
	
	/**
	 * Nombre de demandes terminées de l'usager
	 */
	private Integer terminees;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getEnCours() {
		return enCours;
	}

	public void setEnCours(Integer enCours) {
		this.enCours = enCours;
	}

	public Integer getEnAttenteUsager() {
		return enAttenteUsager;
	}

	public void setEnAttenteUsager(Integer enAttenteUsager) {
		this.enAttenteUsager = enAttenteUsager;
	}

	public Integer getTerminees() {
		return terminees;
	}

	public void setTerminees(Integer terminees) {
		this.terminees = terminees;
	}

}
