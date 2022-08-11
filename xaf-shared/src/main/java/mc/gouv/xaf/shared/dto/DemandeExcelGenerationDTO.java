package mc.gouv.xaf.shared.dto;

import java.util.Map;

/**
 * DTO permettant de spécifier à XAF des paramètres de customisation pour la génération de l'Excel
 * 
 * @author qdeme
 *
 */
public class DemandeExcelGenerationDTO {

	private Map<String, String> buildIdNameMap;
	
	public Map<String, String> getBuildIdNameMap() {
		return buildIdNameMap;
	}

	public void setBuildIdNameMap(Map<String, String> buildIdNameMap) {
		this.buildIdNameMap = buildIdNameMap;
	}
	
}
