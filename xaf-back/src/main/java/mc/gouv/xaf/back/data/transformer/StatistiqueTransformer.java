package mc.gouv.xaf.back.data.transformer;

import org.apache.commons.lang3.StringUtils;

import mc.gouv.xaf.back.data.entity.StatistiqueBO;
import mc.gouv.xaf.shared.dto.StatistiqueDTO;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;

public class StatistiqueTransformer {

    private StatistiqueTransformer() {
    }

    public static StatistiqueDTO bo2Dto(StatistiqueBO bo) {
        if (bo == null) {
            return null;
        }
        StatistiqueDTO dto = new StatistiqueDTO();
        dto.setPkStatistiques(bo.getPkStatistiques());
        dto.setCanal(bo.getCanal());
        dto.setDate(bo.getDate());
        dto.setDemandeId(bo.getDemandeId());
        dto.setDemarcheId(bo.getDemarcheId());
        dto.setStatutPublic(bo.getStatutPublic());
        dto.setIdentifiantDemande(bo.getIdentifiantDemande());
        if (!StringUtils.isEmpty(bo.getTypeConnexionUsager())) {
            dto.setTypeConnexionUsager(TypeConnexionUsagerEnum.valueOf(bo.getTypeConnexionUsager()));
        }
        dto.setOrigine(bo.getOrigine());
        return dto;
    }

    public static StatistiqueBO dto2Bo(StatistiqueDTO dto) {
        if (dto == null) {
            return null;
        }
        StatistiqueBO bo = new StatistiqueBO();
        bo.setPkStatistiques(dto.getPkStatistiques());
        bo.setCanal(dto.getCanal());
        bo.setDate(dto.getDate());
        bo.setDemandeId(dto.getDemandeId());
        bo.setDemarcheId(dto.getDemarcheId());
        bo.setStatutPublic(dto.getStatutPublic());
        bo.setIdentifiantDemande(dto.getIdentifiantDemande());
        if (dto.getTypeConnexionUsager() != null) {
            bo.setTypeConnexionUsager(dto.getTypeConnexionUsager().name());
        }
        bo.setOrigine(dto.getOrigine());
        return bo;
    }

}
