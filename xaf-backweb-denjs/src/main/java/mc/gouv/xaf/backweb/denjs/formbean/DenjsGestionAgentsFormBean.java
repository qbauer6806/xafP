package mc.gouv.xaf.backweb.denjs.formbean;

/**
 * FormBean pour la page de gestion des agents
 * 
 * @author qdeme
 *
 */
public class DenjsGestionAgentsFormBean {

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
