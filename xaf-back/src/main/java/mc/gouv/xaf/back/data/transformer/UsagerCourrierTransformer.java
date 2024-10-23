package mc.gouv.xaf.back.data.transformer;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * @author qdeme
 */
public class UsagerCourrierTransformer {

    private UsagerCourrierTransformer() {
    }

    public static UsagerCourrierDTO bo2Dto(UsagersCourrierBO bo) {
        UsagerCourrierDTO dto = new UsagerCourrierDTO();
        dto.setAdresse1(bo.getAdresse1());
        dto.setAdresse2(bo.getAdresse2());
        dto.setAdresseComplement(bo.getAdresseComplement());
        dto.setCodePostal(bo.getCodePostal());
        dto.setDateCreation(bo.getDateCreation());
        dto.setDateDerModif(bo.getDateDerModif());
        dto.setEmail(bo.getEmail());
        dto.setLogin(bo.getLogin());
        dto.setNom(bo.getNom());
        dto.setPays(bo.getPays());
        dto.setPkUsagersCourrier(bo.getPkUsagersCourrier());
        dto.setPrenom(bo.getPrenom());
        dto.setRaisonSociale(bo.getRaisonSociale());
        dto.setTelephone(bo.getTelephone());
        dto.setTitre(bo.getTitre());
        dto.setVille(bo.getVille());
        return dto;
    }

    public static UsagersCourrierBO dto2Bo(UsagerCourrierDTO dto) {
        UsagersCourrierBO bo = new UsagersCourrierBO();
        bo.setAdresse1(dto.getAdresse1());
        bo.setAdresse2(dto.getAdresse2());
        bo.setAdresseComplement(dto.getAdresseComplement());
        bo.setCodePostal(dto.getCodePostal());
        bo.setDateCreation(dto.getDateCreation());
        bo.setDateDerModif(dto.getDateDerModif());
        bo.setEmail(dto.getEmail());
        bo.setLogin(dto.getLogin());
        bo.setNom(dto.getNom());
        bo.setPays(dto.getPays());
        bo.setPkUsagersCourrier(dto.getPkUsagersCourrier());
        bo.setPrenom(dto.getPrenom());
        bo.setRaisonSociale(dto.getRaisonSociale());
        bo.setTelephone(dto.getTelephone());
        bo.setTitre(dto.getTitre());
        bo.setVille(dto.getVille());
        return bo;
    }

    public static List<UsagerCourrierDTO> bo2Dto(List<UsagersCourrierBO> bos) {
        ArrayList<UsagerCourrierDTO> dtos = new ArrayList<>();
        for (UsagersCourrierBO bo : bos) {
            dtos.add(bo2Dto(bo));
        }
        return dtos;
    }

    public static List<UsagersCourrierBO> dto2Bo(List<UsagerCourrierDTO> dtos) {
        ArrayList<UsagersCourrierBO> bos = new ArrayList<>();
        for (UsagerCourrierDTO dto : dtos) {
            bos.add(dto2Bo(dto));
        }
        return bos;
    }

}
