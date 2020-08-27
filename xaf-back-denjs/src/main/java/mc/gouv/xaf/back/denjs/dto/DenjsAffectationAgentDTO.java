package mc.gouv.xaf.back.denjs.dto;

/**
 * DTO modélisant une affectation entre un agent et un établissement scolaire
 * 
 * @author qdeme
 *
 */
public class DenjsAffectationAgentDTO {

	private String agentMatricule;
	
	private String etablissementCode;

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
	
}
