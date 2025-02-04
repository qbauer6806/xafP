package mc.gouv.xaf.xaf12batch.demandes;

import mc.gouv.xaf.xaf12batch.dto.DemandesAgentsBO;
import mc.gouv.xaf.xaf12batch.logon.dto.User;
import org.springframework.stereotype.Service;

/**
 * 
 * @author qdeme
 *
 */
@Service
public class DemandesAgentsTransformer {

    private DemandesAgentsTransformer() {}

    private String getNomAffichage(User user) {
        if (user != null) {
            char prenom = ' ';
            if (user.getPrenom() != null && user.getPrenom().length() > 1) {
                prenom = user.getPrenom().charAt(0);
            }

            String nom = "";

            if (user.getNomAffichage() != null) {
                nom = user.getNomAffichage();
            }

            return prenom + "." + nom;
        }
        return null;
    }

    public void user2Bo(User user, DemandesAgentsBO bo) {
        if (user != null) {
            bo.setNom(user.getNom());
            bo.setNomUsage(user.getNomUsage());
            bo.setNomNaissance(user.getNomNaissance());
            bo.setPrenom(user.getPrenom());
            bo.setMail(user.getMail());
            bo.setNomAffichage(getNomAffichage(user));
        }
    }
}
