package mc.gouv.xaf.back.service;

/**
 * Service dédié à la gestion des demandes de rectification.
 * @author amdiallo.ext
 */
public interface DemandeRectificationService {

    /**
     * Méthode permettant à l'agent de soumettre une demande de rectification à l'usager.
     *
     * @param pkDemande Identifiant unique de la demande à rectifier.
     * @param commentaire Commentaire accompagnant la demande de rectification.
     */
    void demanderRectification(Integer pkDemande, String commentaire);
}
