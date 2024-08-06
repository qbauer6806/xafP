package mc.gouv.xaf.back.data.transformer;

import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.shared.dto.DemandeAgentDTO;
import org.springframework.stereotype.Service;

/**
 * 
 * @author qdeme
 *
 */
@Service
public class DemandesAgentsTransformer {

    private DemandesAgentsTransformer() {}

    public DemandeAgentDTO bo2Dto(DemandesAgentsBO bo) {
        if (bo == null) {
            return null;
        }
        DemandeAgentDTO dto = new DemandeAgentDTO();
        dto.setId(String.valueOf(bo.getId()));
        dto.setNom(bo.getNom());
        dto.setNomUsage(bo.getNomUsage());
        dto.setNomNaissance(bo.getNomNaissance());
        dto.setPrenom(bo.getPrenom());
        dto.setMail(bo.getMail());
        dto.setNomAffichage(bo.getNomAffichage());
        return dto;
    }

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

    public DemandesAgentsBO user2Bo(User user) {
        if (user == null) {
            return null;
        }
        DemandesAgentsBO bo = new DemandesAgentsBO();
        bo.setId(Integer.valueOf(user.getMatricule()));
        bo.setNom(user.getNom());
        bo.setNomUsage(user.getNomUsage());
        bo.setNomNaissance(user.getNomNaissance());
        bo.setPrenom(user.getPrenom());
        bo.setMail(user.getMail());
        bo.setNomAffichage(getNomAffichage(user));
        return bo;
    }

    public void user2Bo(User user, DemandesAgentsBO bo) {
        bo.setNom(user.getNom());
        bo.setNomUsage(user.getNomUsage());
        bo.setNomNaissance(user.getNomNaissance());
        bo.setPrenom(user.getPrenom());
        bo.setMail(user.getMail());
        bo.setNomAffichage(getNomAffichage(user));
    }
}
