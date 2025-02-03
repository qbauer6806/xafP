package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente une valeur dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenValeurDTO {

	private String code;
	
	private String etat;
	
	private Date dateDebut;
	
	private Date dateFin;
	
	private String libelleCourt;
	
	private String libelleLong;
	
	private Integer ordre;
	
	private List<NomenValeurValeurLienDTO> valeurLiens;
	
	private List<NomenValeurValeurParametreDTO> valeurParametres;
	
}
