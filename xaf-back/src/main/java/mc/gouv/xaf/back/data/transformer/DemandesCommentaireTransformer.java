package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.data.entity.DemandesCommentaireBO;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;

public class DemandesCommentaireTransformer {

    private DemandesCommentaireTransformer() {
    }

    public static DemandeCommentaireDTO bo2Dto(DemandesCommentaireBO bo) {
        DemandeCommentaireDTO dto = new DemandeCommentaireDTO();
        dto.setAgentId(bo.getAgentId());
        dto.setCommentaire(bo.getCommentaire());
        dto.setDate(bo.getDate());
        dto.setFkDemandes(bo.getFkDemandes().getPkDemandes());
        dto.setPkDemandeCommentaire(bo.getPkDemandesCommentaire());
        return dto;
    }

    public static DemandesCommentaireBO dto2Bo(DemandeCommentaireDTO dto) {
        DemandesCommentaireBO bo = new DemandesCommentaireBO();
        bo.setAgentId(dto.getAgentId());
        bo.setCommentaire(dto.getCommentaire());
        bo.setDate(dto.getDate());
        bo.setPkDemandesCommentaire(dto.getPkDemandeCommentaire());
        return bo;
    }

    public static List<DemandeCommentaireDTO> bo2Dto(List<DemandesCommentaireBO> bos) {
        ArrayList<DemandeCommentaireDTO> dtos = new ArrayList<>();
        for (DemandesCommentaireBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<DemandesCommentaireBO> dto2Bo(List<DemandeCommentaireDTO> dtos) {
        ArrayList<DemandesCommentaireBO> bos = new ArrayList<>();
        for (DemandeCommentaireDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
