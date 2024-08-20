package mc.gouv.xaf.xaf12batch.gichuni;

import mc.gouv.xaf.xaf12batch.dto.DemandesUsagersBO;
import org.springframework.stereotype.Service;

/**
 * 
 * @author uek
 *
 */
@Service
public class DemandesUsagersTransformer {

    private DemandesUsagersTransformer() {}

    public void user2Bo(GichuniUsagerDTO user, DemandesUsagersBO bo) {
        if (user != null) {
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
            bo.setTitre((user.getTitre() != null) ? getTitreStr(user.getTitre()) : null);
            bo.setVille(user.getVille());
            bo.setLogin(user.getLogin());
        }
    }

    public String getTitreStr(Short titre) {
        if (titre == null) {
            return "";
        } else if (titre == 0) {
            return "Monsieur";
        } else if (titre == 1) {
            return "Madame";
        } else if (titre == 2) {
            return "Mademoiselle";
        } else {
            return "";
        }
    }
}
