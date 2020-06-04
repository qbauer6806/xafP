package mc.gouv.xaf.back.service.utils;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * Classe utilitaire pour la gestion des usagers. 
 * <br>
 * Les usagers sont les personnes qui font des demandes au niveau du
 * Front-Office. 
 * <br>
 * Leurs données viennent de l'application LOGIN (mc.gouv.login) ou sont
 * ajoutées par les agents pour les courriers.
 * 
 * @author mboutelier.ext
 *
 */
public class UsagersUtils {

    /**
     * Change le titre en paramètre en son abbréviation.
     * 
     * @param titre
     * @return
     */
    public static String titreToAbbreviation(Integer titre) {
        String abbr = null;

        if (titre != null) {
            switch (titre.shortValue()) {
                case AfBackUtils.GENDER_MR_INDEX:
                    abbr = "Mr";
                    break;
                case AfBackUtils.GENDER_MME_INDEX:
                    abbr = "Mme";
                    break;
                case AfBackUtils.GENDER_MLLE_INDEX:
                    abbr = "Mlle";
                    break;
                default:
                    break;
            }
        }

        return abbr;
    }

    /**
     * Change l'abbréviation en paramètre en son titre
     * 
     * @param abbr
     * @return
     */
    public static Integer abbreviationToTitre(String abbr) {
        Integer titre = null;

        if (abbr != null) {
            switch (abbr) {
                case "Mr":
                    titre = (int) AfBackUtils.GENDER_MR_INDEX;
                    break;
                case "Mme":
                    titre = (int) AfBackUtils.GENDER_MME_INDEX;
                    break;
                case "Mlle":
                    titre = (int) AfBackUtils.GENDER_MLLE_INDEX;
                    break;
                default:
                    break;
            }
        }

        return titre;
    }

    public static UsagerBean convertUsagerCourrierDTOToUsagerBean(UsagerCourrierDTO uc) {
        if (uc == null) {
            return null;
        }
        UsagerBean ub = new UsagerBean();
        ub.setAdresse1(uc.getAdresse1());
        ub.setAdresse2(uc.getAdresse2());
        ub.setCodePostal(uc.getCodePostal());
        ub.setComplementAdresse(uc.getAdresseComplement());
        ub.setDateCreation(uc.getDateCreation());
        ub.setEmail(uc.getEmail());
        ub.setId(uc.getPkUsagersCourrier());
        ub.setLogin(uc.getLogin());
        ub.setNom(uc.getNom());
        ub.setPrenom(uc.getPrenom());
        ub.setNomPays(uc.getPays());
        ub.setRaisonSociale(uc.getRaisonSociale());

        if (uc.getTitre() != null) {
            ub.setTitre(uc.getTitre().shortValue());
        }

        ub.setVille(uc.getVille());

        return ub;
    }

}
