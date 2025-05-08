package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class MoyenPaiementTransformer {

    private MoyenPaiementTransformer() {
    }

    public static MoyenPaiementDTO bo2Dto(MoyenPaiementBO bo) {
        MoyenPaiementDTO dto = new MoyenPaiementDTO();
        dto.setPkMoyensPaiements(bo.getPkMoyensPaiements());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerniereModification(bo.getDateDerniereModification());
        dto.setMoyenPaiementStatut(MoyenPaiementStatutTransformer.bo2String(bo.getMoyenPaiementStatut()));
        dto.setPaymentMethodType(bo.getPaymentMethodType());
        dto.setPaymentMethodToken(bo.getPaymentMethodToken());
        dto.setCancellationDate(bo.getCancellationDate());
        dto.setPaymentMethodRecord(bo.getPaymentMethodRecord());
        dto.setPaymentMethodName(bo.getPaymentMethodName());
        dto.setCommande(CommandeTransformer.bo2Dto(bo.getCommande()));
        //TODO dto.setPaymentSupplier(PSPEnumTransformer.bo2String(bo.getPaymentSupplier()));
        return dto;
    }

    public static MoyenPaiementBO dto2Bo(MoyenPaiementDTO dto) {
        MoyenPaiementBO bo = new MoyenPaiementBO();
        bo.setPkMoyensPaiements(dto.getPkMoyensPaiements());
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerniereModification(dto.getDateDerniereModification());
        bo.setMoyenPaiementStatut(MoyenPaiementStatutTransformer.string2Bo(dto.getMoyenPaiementStatut()));
        bo.setPaymentMethodType(dto.getPaymentMethodType());
        bo.setPaymentMethodToken(dto.getPaymentMethodToken());
        bo.setCancellationDate(dto.getCancellationDate());
        bo.setPaymentMethodRecord(dto.getPaymentMethodRecord());
        bo.setPaymentMethodName(dto.getPaymentMethodName());
        bo.setCommande(CommandeTransformer.dto2Bo(dto.getCommande()));
        // TODO bo.setPaymentSupplier(PSPEnumTransformer.string2Bo(dto.getPaymentSupplier()));
        return bo;
    }

    public static List<MoyenPaiementDTO> bos2Dtos(List<MoyenPaiementBO> bos) {
        ArrayList<MoyenPaiementDTO> dtos = new ArrayList<>();
        for (MoyenPaiementBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<MoyenPaiementBO> dtos2Bos(List<MoyenPaiementDTO> dtos) {
        ArrayList<MoyenPaiementBO> bos = new ArrayList<>();
        for (MoyenPaiementDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
