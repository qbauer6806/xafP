package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mboutelier.ext
 */
public class PaiementHistoriqueTransformer {

    private PaiementHistoriqueTransformer() {
    }

    public static PaiementHistoriqueDTO bo2Dto(PaiementHistoriqueBO bo) {
        PaiementHistoriqueDTO dto = new PaiementHistoriqueDTO();
        dto.setPkHistorique(bo.getPkHistorique());
        dto.setContenu(bo.getContenu());
        dto.setDate(bo.getDate().toLocalDateTime());
        dto.setFkDemandes(bo.getFkDemande().getPkDemandes());
        dto.setStatut(PaiementStatutEnum.valueOf(bo.getStatut()));
        dto.setUsagerId(bo.getUsagerId());
        return dto;
    }

    public static PaiementHistoriqueBO dto2Bo(PaiementHistoriqueDTO dto) {
        PaiementHistoriqueBO bo = new PaiementHistoriqueBO();
        bo.setPkHistorique(dto.getPkHistorique());
        bo.setContenu(dto.getContenu());
        bo.setDate(Timestamp.valueOf(dto.getDate()));
        DemandeBO demandeBO = new DemandeBO();
        demandeBO.setPkDemandes(dto.getFkDemandes());
        bo.setFkDemande(demandeBO);
        bo.setStatut(dto.getStatut().name());
        bo.setUsagerId(dto.getUsagerId());
        return bo;
    }

    public static List<PaiementHistoriqueDTO> bos2Dtos(List<PaiementHistoriqueBO> bos) {
        ArrayList<PaiementHistoriqueDTO> dtos = new ArrayList<>();
        for (PaiementHistoriqueBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<PaiementHistoriqueBO> dtos2Bs(List<PaiementHistoriqueDTO> dtos) {
        ArrayList<PaiementHistoriqueBO> bos = new ArrayList<>();
        for (PaiementHistoriqueDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
