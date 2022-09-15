package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author mpavone.ext
 */
public class MoyenPaiementTransformer {

    private MoyenPaiementTransformer() {
    }

    public static MoyenPaiementDTO bo2Dto(MoyenPaiementBO bo) {
        MoyenPaiementDTO dto = new MoyenPaiementDTO();
        dto.setPkMoyenPaiement(bo.getPkMoyenPaiement());
        dto.setCommande(CommandeTransformer.bo2Dto(bo.getCommande()));
        dto.setCodeSociete(bo.getCodeSociete());
        dto.setDateLimite(bo.getDateLimite());
        dto.setMontantInitial(bo.getMontantInitial());
        dto.setMontantCapture(bo.getMontantCapture());
        dto.setMontantRestant(bo.getMontantRestant());
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
        return dto;
    }

    public static MoyenPaiementBO dto2Bo(MoyenPaiementDTO dto) {
        MoyenPaiementBO bo = new MoyenPaiementBO();
        bo.setPkMoyenPaiement(dto.getPkMoyenPaiement());
        bo.setCommande(CommandeTransformer.dto2Bo(dto.getCommande()));
        bo.setCodeSociete(dto.getCodeSociete());
        bo.setDateLimite(dto.getDateLimite());
        bo.setMontantInitial(dto.getMontantInitial());
        bo.setMontantCapture(dto.getMontantCapture());
        bo.setMontantRestant(dto.getMontantRestant());
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


    public static String toCSV(MoyenPaiementDTO moyenPaiementDTO) {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add(moyenPaiementDTO.getPkMoyenPaiement());
        csvString.add(moyenPaiementDTO.getCodeSociete());
        csvString.add(moyenPaiementDTO.getDateLimite().toString());
        csvString.add("" + moyenPaiementDTO.getMontantInitial());
        csvString.add("" + moyenPaiementDTO.getMontantCapture());
        csvString.add("" + moyenPaiementDTO.getMontantRestant());
        csvString.add(moyenPaiementDTO.getMoyenPaiementType() == null ? "null" :moyenPaiementDTO.getMoyenPaiementType());
        csvString.add(moyenPaiementDTO.getMoyenPaiementStatut()== null ? "null" :moyenPaiementDTO.getMoyenPaiementStatut());
        csvString.add(moyenPaiementDTO.getDateDerniereModification().toString());
        csvString.add(moyenPaiementDTO.getCvx());
        csvString.add(moyenPaiementDTO.getVld());
        csvString.add(moyenPaiementDTO.getBrand());
        csvString.add(moyenPaiementDTO.getNumauto());
        csvString.add(moyenPaiementDTO.getUsage());
        csvString.add(moyenPaiementDTO.getTypecompte());
        csvString.add(moyenPaiementDTO.getEcard());
        csvString.add(moyenPaiementDTO.getCbmasquee());
        csvString.add(moyenPaiementDTO.getOriginecb());
        csvString.add(moyenPaiementDTO.getBincb());
        csvString.add(moyenPaiementDTO.getHpancb());
        csvString.add(moyenPaiementDTO.getIpclient());
        csvString.add(moyenPaiementDTO.getOriginetr());
        csvString.add(moyenPaiementDTO.getModepaiement());
        csvString.add(moyenPaiementDTO.getAuthentification());
        csvString.add(moyenPaiementDTO.getLangue());
        return csvString.toString();
    }

    public static String headerCSV() {
        StringJoiner csvString = new StringJoiner(";");
        csvString.add("pkMoyenPaiement");
        csvString.add("codeSociete");
        csvString.add("dateLimite");
        csvString.add("montantInitial");
        csvString.add("montantCapture");
        csvString.add("montantRestant");
        csvString.add("moyenPaiementType");
        csvString.add("moyenPaiementStatut");
        csvString.add("dateDerniereModification");
        csvString.add("cvx");
        csvString.add("vld");
        csvString.add("brand");
        csvString.add("numauto");
        csvString.add("usage");
        csvString.add("typecompte");
        csvString.add("ecard");
        csvString.add("cbmasquee");
        csvString.add("originecb");
        csvString.add("bincb");
        csvString.add("hpancb");
        csvString.add("ipclient");
        csvString.add("originetr");
        csvString.add("modepaiement");
        csvString.add("authentification");
        csvString.add("langue");
        return csvString.toString();
    }

}
