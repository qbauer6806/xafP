package mc.gouv.xaf.front.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente le retour d'un appel à l'API NOMEN (simplifié)
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureDTO {
	
	private List<NomenValeurDTO> valeurs;
	
}
