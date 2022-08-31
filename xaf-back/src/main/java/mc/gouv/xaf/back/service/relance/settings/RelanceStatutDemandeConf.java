package mc.gouv.xaf.back.service.relance.settings;

import java.io.Serializable;

/**
 * 
 * 
 * Classe permettant de créer des conf de statut à expirer en spécifiant 
 *  - Le statut à relancer
 *  - Le délai avant la 1ere relance
 *  - Le délai entre 2 relances
 *  - Le prefix de la clef mail à utiliser (ie MAIL_EN_ATTENT_COMPL)
 * @author XDECOOL.EXT
 *
 */
public class RelanceStatutDemandeConf implements Serializable {

	private static final long serialVersionUID = 6123329536305326942L;
	private String statutARelancer;
	private Integer delaiAvantPremiereRelance;
	private Integer delaiEntreDeuxRelances;
	private String clefMailPrefix;
	
	public RelanceStatutDemandeConf(String statutARelancer, Integer delaiAvantPremiereRelance, Integer delaiEntreDeuxRelances, String clefMailPrefix) {
		this.statutARelancer = statutARelancer;
		this.delaiAvantPremiereRelance = delaiAvantPremiereRelance;
		this.delaiEntreDeuxRelances = delaiEntreDeuxRelances;
		this.clefMailPrefix = clefMailPrefix;
	}
	
	public String getStatutARelancer() {
		return statutARelancer;
	}

	public void setStatutARelancer(String statutARelancer) {
		this.statutARelancer = statutARelancer;
	}

	public Integer getDelaiAvantPremiereRelance() {
		return delaiAvantPremiereRelance;
	}

	public void setDelaiAvantPremiereRelance(Integer delaiAvantPremiereRelance) {
		this.delaiAvantPremiereRelance = delaiAvantPremiereRelance;
	}

	public Integer getDelaiEntreDeuxRelances() {
		return delaiEntreDeuxRelances;
	}

	public void setDelaiEntreDeuxRelances(Integer delaiEntreDeuxRelances) {
		this.delaiEntreDeuxRelances = delaiEntreDeuxRelances;
	}

	public String getClefMailPrefix() {
		return clefMailPrefix;
	}

	public void setClefMailPrefix(String clefMailPrefix) {
		this.clefMailPrefix = clefMailPrefix;
	}
}
