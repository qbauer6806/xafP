package mc.gouv.xaf.front.dto;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe rassemblant toutes les informations de token OIDC (GICHKEY/Keycloak)
 * 
 * @author qdeme
 * 
 */
@Setter
@Getter
public class KeycloakTokenInfo implements Serializable {

	private static final long serialVersionUID = -5604923412541200687L;

	private String accessToken;
	
	private Integer expiresIn;
	
	private String refreshToken;
	
	private Integer refreshExpiresIn;
	
	private String tokenType;
	
	private Integer notBeforePolicy;
	
	private String sessionState;
	
	private String scope;
	
	private Date dateObtention;

}
