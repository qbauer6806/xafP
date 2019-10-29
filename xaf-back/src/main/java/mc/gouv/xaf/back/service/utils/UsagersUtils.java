package mc.gouv.xaf.back.service.utils;

import org.springframework.stereotype.Component;

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
@Component
public class UsagersUtils {

    /**
     * Change le titre en paramètre en son abbréviation.
     * 
     * @param titre
     * @return
     */
    public String titreToAbbreviation(Integer titre) {
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
    public Integer abbreviationToTitre(String abbr) {
        Integer titre = null;

        if (abbr != null) {
            switch (abbr) {
                case "Mr":
                    titre = Integer.valueOf(AfBackUtils.GENDER_MR_INDEX);
                    break;
                case "Mme":
                    titre = Integer.valueOf(AfBackUtils.GENDER_MME_INDEX);
                    break;
                case "Mlle":
                    titre = Integer.valueOf(AfBackUtils.GENDER_MLLE_INDEX);
                    break;
                default:
                    break;
            }
        }

        return titre;
    }

}
