package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.RestitutionStatistiquesBO;
import mc.gouv.xaf.shared.dto.RestitutionStatistiquesDTO;

public class RestitutionStatistiquesTransformer {

    private RestitutionStatistiquesTransformer() {
    }
    
    public static RestitutionStatistiquesDTO bo2Dto(RestitutionStatistiquesBO bo) {
        if (bo == null) {
            return null;
        }
        RestitutionStatistiquesDTO dto = new RestitutionStatistiquesDTO();
        dto.setPkStatistique(bo.getPkStatistique());
        dto.setUsagerId(bo.getUsagerId());
        dto.setHttpCode(bo.getHttpCode());
        dto.setMessage(bo.getMessage());
        dto.setDate(bo.getDate());
        dto.setNom(bo.getNom());
        dto.setPrenoms(bo.getPrenoms());
        dto.setDateNaissance(bo.getDateNaissance());
        dto.setHeureNaissance(bo.getHeureNaissance());
        dto.setVilleNaissance(bo.getVilleNaissance());
        dto.setPaysNaissance(bo.getPaysNaissance());
        return dto;
    }
    
    public static RestitutionStatistiquesBO dto2Bo(RestitutionStatistiquesDTO dto) {
        if (dto == null) {
            return null;
        }
        RestitutionStatistiquesBO bo = new RestitutionStatistiquesBO();
        bo.setPkStatistique(dto.getPkStatistique());
        bo.setUsagerId(dto.getUsagerId());
        bo.setHttpCode(dto.getHttpCode());
        bo.setMessage(dto.getMessage());
        bo.setDate(dto.getDate());
        bo.setNom(dto.getNom());
        bo.setPrenoms(dto.getPrenoms());
        bo.setDateNaissance(dto.getDateNaissance());
        bo.setHeureNaissance(dto.getHeureNaissance());
        bo.setVilleNaissance(dto.getVilleNaissance());
        bo.setPaysNaissance(dto.getPaysNaissance());
        return bo;
    }
    
}
