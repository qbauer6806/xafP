package mc.gouv.xaf.backweb.denjs.dto;

/**
 * DTO pour affichage des données du tableau de la page de gestion des agents
 * 
 * @author qdeme
 *
 */
public class DenjsAgentEtablissementDTO {
	
	private String agentNom;
	
	private String agentMatricule;
	
	private String etablissementCode;
	
	private String etablissementNom;

	public String getAgentNom() {
		return agentNom;
	}

	public void setAgentNom(String agentNom) {
		this.agentNom = agentNom;
	}

	public String getAgentMatricule() {
		return agentMatricule;
	}

	public void setAgentMatricule(String agentMatricule) {
		this.agentMatricule = agentMatricule;
	}

	public String getEtablissementCode() {
		return etablissementCode;
	}

	public void setEtablissementCode(String etablissementCode) {
		this.etablissementCode = etablissementCode;
	}

	public String getEtablissementNom() {
		return etablissementNom;
	}

	public void setEtablissementNom(String etablissementNom) {
		this.etablissementNom = etablissementNom;
	}

}
