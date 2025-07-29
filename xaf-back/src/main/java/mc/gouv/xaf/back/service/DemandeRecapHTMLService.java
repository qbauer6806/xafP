package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une demande.
 *
 * @author qdeme
 * @author mboutelier.ext
 */
public interface DemandeRecapHTMLService {

    /**
     * Méthode générant la partie d'informations de la demande de la page.
     */
    String getHTMLDemandeGeneric(DemandeDTO demande);

    /**
     * Méthode générant la partie des informations complémentaires de la demande.
     */
    String getHTMLDemandeComplements(DemandeDTO demande);

    /**
     * Méthode générant la partie demande initiale de la page, elle est générée en utilisant le fichier JSON recap
     * implémenté par le WISYWIG
     */
    String getHTMLDemandeContenuRecap(DemandeDTO demande, boolean isPdfRecap)
            throws IllegalArgumentException, SecurityException;

}
