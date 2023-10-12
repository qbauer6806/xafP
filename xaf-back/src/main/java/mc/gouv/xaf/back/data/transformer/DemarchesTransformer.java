package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

/**
 * 
 * @author qdeme
 *
 */
public class DemarchesTransformer {
    
    private DemarchesTransformer() {
    }

    public static DemarcheDTO bo2Dto(DemarchesBO bo) {
        DemarcheDTO dto = new DemarcheDTO();
        dto.setPkDemarches(bo.getPkDemarches());
        dto.setNom(bo.getNom());
        dto.setNomEn(bo.getNomEn());
        dto.setEmailService(bo.getEmailService());
        dto.setEmailServiceNom(bo.getEmailServiceNom());
        dto.setEmailReplyto(bo.getEmailReplyto());
        dto.setEmailReplytoNom(bo.getEmailReplytoNom());
        dto.setEmailFrom(bo.getEmailFrom());
        dto.setEmailFromNom(bo.getEmailFromNom());
        dto.setIdentifiantPrefixe(bo.getIdentifiantPrefixe());
        dto.setLangues(bo.getLangues());
        return dto;
    }

    public static DemarchesBO dto2Bo(DemarcheDTO dto) {
        DemarchesBO bo = new DemarchesBO();
        bo.setPkDemarches(dto.getPkDemarches());
        bo.setNom(dto.getNom());
        bo.setNomEn(dto.getNomEn());
        bo.setEmailService(dto.getEmailService());
        bo.setEmailServiceNom(dto.getEmailServiceNom());
        bo.setEmailReplyto(dto.getEmailReplyto());
        bo.setEmailReplytoNom(dto.getEmailReplytoNom());
        bo.setEmailFrom(dto.getEmailFrom());
        bo.setEmailFromNom(dto.getEmailFromNom());
        bo.setIdentifiantPrefixe(dto.getIdentifiantPrefixe());
        bo.setLangues(dto.getLangues());
        return bo;
    }
    
    public static List<DemarcheDTO> bo2Dto(List<DemarchesBO> bos) {
        ArrayList<DemarcheDTO> dtos = new ArrayList<>();
        for (DemarchesBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }
    
    public static List<DemarchesBO> dto2Bo(List<DemarcheDTO> dtos) {
        ArrayList<DemarchesBO> bos = new ArrayList<>();
        for (DemarcheDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }
    
}
