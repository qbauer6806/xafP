package mc.gouv.xaf.back.service.expiration.settings;

import org.springframework.stereotype.Component;

@Component
public class ExpirationDemandeSettings {

	private String statutAExpirer;
	private Integer delaiExpiration;
	private String clefMailPrefix;
	
	public ExpirationDemandeSettings(String statutAExpirer, Integer delaiExpiration, String clefMailPrefix) {
		this.statutAExpirer = statutAExpirer;
		this.delaiExpiration = delaiExpiration;
		this.clefMailPrefix = clefMailPrefix;
	}

	public String getStatutAExpirer() {
		return statutAExpirer;
	}

	public void setStatutAExpirer(String statutAExpirer) {
		this.statutAExpirer = statutAExpirer;
	}

	public Integer getDelaiExpiration() {
		return delaiExpiration;
	}

	public void setDelaiExpiration(Integer delaiExpiration) {
		this.delaiExpiration = delaiExpiration;
	}

	public String getClefMailPrefix() {
		return clefMailPrefix;
	}

	public void setClefMailPrefix(String clefMailPrefix) {
		this.clefMailPrefix = clefMailPrefix;
	}
	
}
