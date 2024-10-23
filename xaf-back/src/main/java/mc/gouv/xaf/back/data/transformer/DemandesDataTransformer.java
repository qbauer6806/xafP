package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

/**
 * @author qdeme
 */
public class DemandesDataTransformer {

    private DemandesDataTransformer() {
    }

    public static DemandeDataDTO bo2Dto(DemandesDataBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeDataDTO dto = new DemandeDataDTO();
        dto.setDemandeId(bo.getFkDemandes().getPkDemandes());
        dto.setKey(bo.getKey());
        dto.setValue(bo.getValue());
        dto.setPkDemandesData(bo.getPkDemandesData());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction
     */
    public static DemandesDataBO dto2Bo(DemandeDataDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandesDataBO bo = new DemandesDataBO();
        bo.setKey(dto.getKey());
        bo.setValue(dto.getValue());
        bo.setPkDemandesData(dto.getPkDemandesData());
        return bo;
    }

    public static List<DemandeDataDTO> bo2Dto(List<DemandesDataBO> bos) {
        ArrayList<DemandeDataDTO> dtos = new ArrayList<>();
        for (DemandesDataBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static Set<DemandeDataDTO> bo2Dto(Set<DemandesDataBO> bos) {
        Set<DemandeDataDTO> dtos = new HashSet<>();
        for (DemandesDataBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesDataBO> dto2Bo(List<DemandeDataDTO> dtos) {
        ArrayList<DemandesDataBO> bos = new ArrayList<>();
        for (DemandeDataDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
