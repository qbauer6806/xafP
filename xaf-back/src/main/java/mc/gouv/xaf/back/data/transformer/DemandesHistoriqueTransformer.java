package mc.gouv.xaf.back.data.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.back.data.entity.DemandesHistoriqueBO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemandesHistoriqueTransformer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesHistoriqueTransformer.class);

    private DemandesHistoriqueTransformer() {
    }
    
    public static DemandeHistoriqueDTO bo2Dto(DemandesHistoriqueBO bo) {
        DemandeHistoriqueDTO dto = new DemandeHistoriqueDTO();
        dto.setAgentId(bo.getAgentId());
        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setContenu(mapper.readTree(bo.getContenu()));
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        dto.setDate(bo.getDate());
        dto.setFkDemandes(bo.getFkDemandes().getPkDemandes());
        dto.setFkStatut(DemandesStatutsTransformer.bo2Dto(bo.getFkStatut()));
        dto.setPkDemandeHistorique(bo.getPkDemandesHistorique());
        dto.setUsagerId(bo.getUsagerId());
        dto.setJustificatifTraitement(bo.getJustificatifTraitement());
        return dto;
    }
    
    /**
     * Cette entité est à rattacher à une demande et à un statut de demande
     * après appel à cette fonction
     * @param dto
     * @return
     */
    public static DemandesHistoriqueBO dto2Bo(DemandeHistoriqueDTO dto) {
        DemandesHistoriqueBO bo = new DemandesHistoriqueBO();
        bo.setAgentId(dto.getAgentId());
        ObjectMapper mapper = new ObjectMapper();
        try {
            bo.setContenu(mapper.writeValueAsString(dto.getContenu()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Erreur lors de la conversion JSON", e);
        }
        bo.setDate(dto.getDate());
        bo.setPkDemandesHistorique(dto.getPkDemandeHistorique());
        bo.setUsagerId(dto.getUsagerId());
        bo.setJustificatifTraitement(dto.getJustificatifTraitement());
        return bo;
    }
    
    public static List<DemandeHistoriqueDTO> bo2Dto(List<DemandesHistoriqueBO> bos) {
        ArrayList<DemandeHistoriqueDTO> dtos = new ArrayList<DemandeHistoriqueDTO>();
        for (DemandesHistoriqueBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<DemandesHistoriqueBO> dto2Bo(List<DemandeHistoriqueDTO> dtos) {
        ArrayList<DemandesHistoriqueBO> bos = new ArrayList<DemandesHistoriqueBO>();
        for (DemandeHistoriqueDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
