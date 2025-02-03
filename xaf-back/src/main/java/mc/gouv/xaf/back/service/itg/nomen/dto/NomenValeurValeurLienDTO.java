package mc.gouv.xaf.back.service.itg.nomen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un lien dans une valeur d'une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenValeurValeurLienDTO {

	private String lienNomenclatureCode;
	
	private String lienValeurCode;
	
	private String lienValeurLibelle;
	
}
