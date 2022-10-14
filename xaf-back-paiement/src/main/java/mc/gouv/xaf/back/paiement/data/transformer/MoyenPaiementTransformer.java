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
        dto.setPkMoyenPaiements(bo.getPkMoyensPaiements());
        dto.setCodeSociete(bo.getCodeSociete());
        dto.setDateLimite(bo.getDateLimite());
        dto.setMoyenPaiementType(MoyenPaiementTypeTransformer.bo2String(bo.getMoyenPaiementType()));
        dto.setMoyenPaiementStatut(MoyenPaiementStatutTransformer.bo2String(bo.getMoyenPaiementStatut()));
        dto.setDateDerniereModification(bo.getDateDerniereModification());
        dto.setCvx(bo.getCvx());
        dto.setVld(bo.getVld());
        dto.setBrand(bo.getBrand());
        dto.setNumauto(bo.getNumauto());
        dto.setUsage(bo.getUsage());
        dto.setTypecompte(bo.getTypecompte());
        dto.setEcard(bo.getEcard());
        dto.setOriginecb(bo.getOriginecb());
        dto.setCbmasquee(bo.getCbmasquee());
        dto.setBincb(bo.getBincb());
        dto.setHpancb(bo.getHpancb());
        dto.setIpclient(bo.getIpclient());
        dto.setOriginetr(bo.getOriginetr());
        dto.setModepaiement(bo.getModepaiement());
        dto.setAuthentification(bo.getAuthentification());
        dto.setLangue(bo.getLangue());
        dto.setMac(bo.getMac());
        return dto;
    }

    public static MoyenPaiementBO dto2Bo(MoyenPaiementDTO dto) {
        MoyenPaiementBO bo = new MoyenPaiementBO();
        bo.setPkMoyensPaiements(dto.getPkMoyenPaiements());
        bo.setCodeSociete(dto.getCodeSociete());
        bo.setDateLimite(dto.getDateLimite());
        bo.setMoyenPaiementType(MoyenPaiementTypeTransformer.string2Bo(dto.getMoyenPaiementType()));
        bo.setMoyenPaiementStatut(MoyenPaiementStatutTransformer.string2Bo(dto.getMoyenPaiementStatut()));
        bo.setDateDerniereModification(dto.getDateDerniereModification());
        bo.setCvx(dto.getCvx());
        bo.setVld(dto.getVld());
        bo.setBrand(dto.getBrand());
        bo.setNumauto(dto.getNumauto());
        bo.setUsage(dto.getUsage());
        bo.setTypecompte(dto.getTypecompte());
        bo.setEcard(dto.getEcard());
        bo.setOriginecb(dto.getOriginecb());
        bo.setCbmasquee(dto.getCbmasquee());
        bo.setBincb(dto.getBincb());
        bo.setHpancb(dto.getHpancb());
        bo.setIpclient(dto.getIpclient());
        bo.setOriginetr(dto.getOriginetr());
        bo.setModepaiement(dto.getModepaiement());
        bo.setAuthentification(dto.getAuthentification());
        bo.setLangue(dto.getLangue());
        bo.setMac(dto.getMac());
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
