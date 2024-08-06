package mc.gouv.xaf.back.denjs.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO modélisant une affectation entre un agent et un établissement scolaire
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class DenjsAffectationAgentDTO {

	private String agentMatricule;
	
	private String etablissementCode;

}
