package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.StatistiquesTypesBO;
import mc.gouv.xaf.shared.dto.StatistiquesTypesDTO;

public class StatistiquesTypesTransformer {
	
	private StatistiquesTypesTransformer() {
	}
	
	public static StatistiquesTypesDTO bo2Dto(StatistiquesTypesBO bo) {
		if (bo == null) {
            return null;
        }
        StatistiquesTypesDTO dto = new StatistiquesTypesDTO();
        dto.setPkStatistiquesTypes(bo.getPkStatistiquesTypes());
        dto.setIdentifiantDemande(bo.getIdentifiantDemande());
        dto.setValue(bo.getValue());
        return dto;
	}
	
	public static StatistiquesTypesBO dto2Bo(StatistiquesTypesDTO dto) {
        if (dto == null) {
            return null;
        }
        StatistiquesTypesBO bo = new StatistiquesTypesBO();
        bo.setPkStatistiquesTypes(dto.getPkStatistiquesTypes());
        bo.setIdentifiantDemande(dto.getIdentifiantDemande());
        bo.setValue(dto.getValue());
        return bo;
	}

}
