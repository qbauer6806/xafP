package mc.gouv.xaf.servlet.dto;

import java.util.Date;

/**
 * 
 * Classe rassemblant toutes les informations de token OIDC (GICHKEY/Keycloak)
 * 
 * @author qdeme
 * 
 */
public class KeycloakTokenInfo {
	
	private String accessToken;
	
	private Integer expiresIn;
	
	private String refreshToken;
	
	private Integer refreshExpiresIn;
	
	private String tokenType;
	
	private Integer notBeforePolicy;
	
	private String sessionState;
	
	private String scope;
	
	private Date dateObtention;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Integer getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Integer getRefreshExpiresIn() {
		return refreshExpiresIn;
	}

	public void setRefreshExpiresIn(Integer refreshExpiresIn) {
		this.refreshExpiresIn = refreshExpiresIn;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public Integer getNotBeforePolicy() {
		return notBeforePolicy;
	}

	public void setNotBeforePolicy(Integer notBeforePolicy) {
		this.notBeforePolicy = notBeforePolicy;
	}

	public String getSessionState() {
		return sessionState;
	}

	public void setSessionState(String sessionState) {
		this.sessionState = sessionState;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Date getDateObtention() {
		return dateObtention;
	}

	public void setDateObtention(Date dateObtention) {
		this.dateObtention = dateObtention;
	}

}
