package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qdeme
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
        dto.setEmailReplyto(bo.getEmailReplyto());
        dto.setEmailReplytoNom(bo.getEmailReplytoNom());
        dto.setEmailFrom(bo.getEmailFrom());
        dto.setEmailFromNom(bo.getEmailFromNom());
        dto.setNomDirection(bo.getNomDirection());
        dto.setNomSousDirection(bo.getNomSousDirection());
        dto.setNomFooter(bo.getNomFooter());
        dto.setAdresseService(bo.getAdresseService());
        dto.setIdentifiantPrefixe(bo.getIdentifiantPrefixe());
        dto.setLangues(bo.getLangues());
        dto.setNomSousDirectionComplement(bo.getNomSousDirectionComplement());
        dto.setTelephoneService(bo.getTelephoneService());
        dto.setNomDirectionEn(bo.getNomDirectionEn());
        dto.setNomSousDirectionEn(bo.getNomSousDirectionEn());
        dto.setNomSousDirectionComplementEn(bo.getNomSousDirectionComplementEn());
        return dto;
    }

    public static DemarchesBO dto2Bo(DemarcheDTO dto) {
        DemarchesBO bo = new DemarchesBO();
        bo.setPkDemarches(dto.getPkDemarches());
        bo.setNom(dto.getNom());
        bo.setNomEn(dto.getNomEn());
        bo.setEmailService(dto.getEmailService());
        bo.setEmailReplyto(dto.getEmailReplyto());
        bo.setEmailReplytoNom(dto.getEmailReplytoNom());
        bo.setEmailFrom(dto.getEmailFrom());
        bo.setEmailFromNom(dto.getEmailFromNom());
        bo.setNomDirection(dto.getNomDirection());
        bo.setNomSousDirection(dto.getNomSousDirection());
        bo.setNomFooter(dto.getNomFooter());
        bo.setAdresseService(dto.getAdresseService());
        bo.setIdentifiantPrefixe(dto.getIdentifiantPrefixe());
        bo.setLangues(dto.getLangues());
        bo.setNomSousDirectionComplement(dto.getNomSousDirectionComplement());
        bo.setTelephoneService(dto.getTelephoneService());
        bo.setNomDirectionEn(dto.getNomDirectionEn());
        bo.setNomSousDirectionEn(dto.getNomSousDirectionEn());
        bo.setNomSousDirectionComplementEn(dto.getNomSousDirectionComplementEn());
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
