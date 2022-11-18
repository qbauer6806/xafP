package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.web.servlet.ModelAndView;

/**
 * Service permettant de réupérer les données des paiements à afficher dans une demande
 *
 * @author mboutelier.ext
 */
public interface DonneesPaiementsService {

    /**
     * Charge dans le MAV les données liées au Paiement
     */
    void chargerDonneesPaiement(ModelAndView mav, DemandeDTO demande);

}
