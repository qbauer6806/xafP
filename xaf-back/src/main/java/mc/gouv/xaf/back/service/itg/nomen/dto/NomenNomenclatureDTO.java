package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente le retour d'un appel à l'API NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureDTO {

	private String code;
	
	private String type;
	
	private String categorie;
	
	private String etat;
	
	private Date dateFin;
	
	private String version;
	
	private Date versionDate;
	
	private String source;
	
	private String serviceResponsableCode;
	
	private String titre;
	
	private String description;
	
	private String remarques;
	
	private List<NomenNomenclatureLienDTO> nomenclatureLiens;
	
	private List<NomenNomenclatureLocaleDTO> nomenclatureLocales;
	
	private List<NomenNomenclatureParametreDTO> nomenclatureParametres;
	
	private List<NomenValeurDTO> valeurs;
	
}
