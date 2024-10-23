package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qdeme
 */
public class DemandesStatutsTransformer {

    private DemandesStatutsTransformer() {
    }

    public static DemandeStatutDTO bo2Dto(DemandesStatutsBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeStatutDTO dto = new DemandeStatutDTO();
        dto.setPkStatut(bo.getPkDemandesStatuts());
        dto.setLibelle(bo.getLibelle());
        dto.setName(bo.getName());
        dto.setDate(bo.getDate());
        dto.setAgentId(bo.getAgentId());
        dto.setUsagerId(bo.getUsagerId());
        dto.setCodeMotif(bo.getCodeMotif());
        dto.setCommentaire(bo.getCommentaire());
        dto.setTexteAEnvoyer(bo.getTexteAEnvoyer());
        return dto;
    }

    /**
     * L'entité retournée est à rattacher à un DemandeBO après l'appel à cette fonction
     */
    public static DemandesStatutsBO dto2Bo(DemandeStatutDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandesStatutsBO bo = new DemandesStatutsBO();
        bo.setLibelle(dto.getLibelle());
        bo.setName(dto.getName());
        bo.setDate(dto.getDate());
        bo.setAgentId(dto.getAgentId());
        bo.setUsagerId(dto.getUsagerId());
        bo.setCodeMotif(dto.getCodeMotif());
        bo.setCommentaire(dto.getCommentaire());
        bo.setTexteAEnvoyer(dto.getTexteAEnvoyer());
        return bo;
    }

    public static List<DemandeStatutDTO> bo2Dto(List<DemandesStatutsBO> bos) {
        ArrayList<DemandeStatutDTO> dtos = new ArrayList<>();
        for (DemandesStatutsBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static Set<DemandeStatutDTO> bo2Dto(Set<DemandesStatutsBO> bos) {
        Set<DemandeStatutDTO> dtos = new HashSet<>();
        for (DemandesStatutsBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesStatutsBO> dto2Bo(List<DemandeStatutDTO> dtos) {
        ArrayList<DemandesStatutsBO> bos = new ArrayList<>();
        for (DemandeStatutDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
