package mc.gouv.xaf.front.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;


/**
 * Version simplifiée de NomenValeurDTO pour le FO
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenValeurDTO {

	private String code;
	
	private String libelleCourt;
	
	private String libelleLong;
	
	private Integer ordre;
	
}
