package mc.gouv.xaf.back.service.itg.nomen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un paramètre dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureParametreDTO {

	private String parametreNom;
	
	private String parametreType;
	
	private boolean parametreObligatoire;
	
	private Integer parametreLongueur;
	
	private String parametreFormat;

}
