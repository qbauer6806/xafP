package mc.gouv.xaf.back.service.relance.settings;

import org.springframework.stereotype.Component;

@Component
public class RelanceDemandeSettings {

	private String statutARelancer;
	private Integer delaiAvantPremiereRelance;
	private Integer delaiEntreDeuxRelances;
	private String clefMailPrefix;
	
	public RelanceDemandeSettings(String statutARelancer, Integer delaiAvantPremiereRelance, Integer delaiEntreDeuxRelances, String clefMailPrefix) {
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
