package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.springframework.stereotype.Service;

/**
 * @author uek
 */
@Service
public class DemandesUsagersTransformer {

    private DemandesUsagersTransformer() {
    }

    public DemandesUsagersBO dto2Bo(DemandeUsagerDTO dto) {
        if (dto == null) {
            return null;
        }
        DemandesUsagersBO bo = new DemandesUsagersBO();
        bo.setAdresse1(dto.getAdresse1());
        bo.setAdresse2(dto.getAdresse2());
        bo.setCodePostal(dto.getCodePostal());
        bo.setComplementAdresse(dto.getComplementAdresse());
        bo.setEmail(dto.getEmail());
        bo.setEtat(dto.getEtat());
        bo.setNom(dto.getNom());
        bo.setNomPays(dto.getNomPays());
        bo.setPrenom(dto.getPrenom());
        bo.setRaisonSociale(dto.getRaisonSociale());
        bo.setTitre(dto.getTitre());
        bo.setVille(dto.getVille());
        bo.setLogin(dto.getLogin());
        bo.setId(dto.getId());
        return bo;
    }

    public DemandeUsagerDTO bo2Dto(DemandesUsagersBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeUsagerDTO dto = new DemandeUsagerDTO();
        dto.setAdresse1(bo.getAdresse1());
        dto.setAdresse2(bo.getAdresse2());
        dto.setCodePostal(bo.getCodePostal());
        dto.setComplementAdresse(bo.getComplementAdresse());
        dto.setEmail(bo.getEmail());
        dto.setEtat(bo.getEtat());
        dto.setNom(bo.getNom());
        dto.setNomPays(bo.getNomPays());
        dto.setPrenom(bo.getPrenom());
        dto.setRaisonSociale(bo.getRaisonSociale());
        dto.setTitre(bo.getTitre());
        dto.setVille(bo.getVille());
        dto.setLogin(bo.getLogin());
        dto.setId(bo.getId());
        return dto;
    }

    public DemandeUsagerDTO user2Dto(GichuniUsagerDTO user) {
        if (user == null) {
            return null;
        }
        DemandeUsagerDTO dto = new DemandeUsagerDTO();
        dto.setAdresse1(user.getAdresse1());
        dto.setAdresse2(user.getAdresse2());
        dto.setCodePostal(user.getCodePostal());
        dto.setComplementAdresse(user.getComplementAdresse());
        dto.setEmail(user.getEmail());
        dto.setEtat(user.getEtat() != null ? user.getEtat().toString() : null);
        dto.setNom(user.getNom());
        dto.setNomPays(user.getNomPays());
        dto.setPrenom(user.getPrenom());
        dto.setRaisonSociale(user.getRaisonSociale());
        dto.setTitre((user.getTitre() != null) ? AfBackUtils.getTitreStr(user.getTitre()) : null);
        dto.setVille(user.getVille());
        dto.setLogin(user.getLogin());
        dto.setId(user.getId());
        return dto;
    }

    public DemandesUsagersBO user2Bo(GichuniUsagerDTO user) {
        if (user == null) {
            return null;
        }
        DemandesUsagersBO bo = new DemandesUsagersBO();
        bo.setAdresse1(user.getAdresse1());
        bo.setAdresse2(user.getAdresse2());
        bo.setCodePostal(user.getCodePostal());
        bo.setComplementAdresse(user.getComplementAdresse());
        bo.setEmail(user.getEmail());
        bo.setEtat(user.getEtat() != null ? user.getEtat().toString() : null);
        bo.setNom(user.getNom());
        bo.setNomPays(user.getNomPays());
        bo.setPrenom(user.getPrenom());
        bo.setRaisonSociale(user.getRaisonSociale());
        bo.setTitre((user.getTitre() != null) ? AfBackUtils.getTitreStr(user.getTitre()) : null);
        bo.setVille(user.getVille());
        bo.setLogin(user.getLogin());
        bo.setId(user.getId());
        return bo;
    }
}
