package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesHistoriqueTransformer {

    private DemandesHistoriqueTransformer() {
    }
    
    public static DemandeHistoriqueDTO bo2Dto(DemandesHistoriqueBO bo) {
        DemandeHistoriqueDTO dto = new DemandeHistoriqueDTO();
        dto.setAgentId(bo.getAgentId());
        dto.setContenu(bo.getContenu());
        dto.setDate(bo.getDate());
        dto.setFkDemandes(bo.getFkDemandes().getPkDemandes());
        dto.setFkStatut(DemandesStatutsTransformer.bo2Dto(bo.getFkStatut()));
        dto.setPkDemandeHistorique(bo.getPkDemandesHistorique());
        dto.setUsagerId(bo.getUsagerId());
        dto.setJustificatifTraitement(bo.getJustificatifTraitement());
        return dto;
    }
    
    /**
     * Cette entité est à rattacher à une demande et à un statut de demande après appel à cette fonction
     */
    public static DemandesHistoriqueBO dto2Bo(DemandeHistoriqueDTO dto) {
        DemandesHistoriqueBO bo = new DemandesHistoriqueBO();
        bo.setAgentId(dto.getAgentId());
        bo.setContenu(dto.getContenu());
        bo.setDate(dto.getDate());
        bo.setPkDemandesHistorique(dto.getPkDemandeHistorique());
        bo.setUsagerId(dto.getUsagerId());
        bo.setJustificatifTraitement(dto.getJustificatifTraitement());
        return bo;
    }
    
    public static List<DemandeHistoriqueDTO> bo2Dto(List<DemandesHistoriqueBO> bos) {
        ArrayList<DemandeHistoriqueDTO> dtos = new ArrayList<>();
        for (DemandesHistoriqueBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<DemandesHistoriqueBO> dto2Bo(List<DemandeHistoriqueDTO> dtos) {
        ArrayList<DemandesHistoriqueBO> bos = new ArrayList<>();
        for (DemandeHistoriqueDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
