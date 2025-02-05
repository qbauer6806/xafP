package mc.gouv.xaf.back.service.itg.nomen.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un lien dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureLienDTO {

	private String lienCode;
	
	private String lienDescription;

	@Override
	public String toString() {
		return "NomenNomenclatureLienDTO [lienCode=" + lienCode + ", lienDescription=" + lienDescription + "]";
	}
	
}
