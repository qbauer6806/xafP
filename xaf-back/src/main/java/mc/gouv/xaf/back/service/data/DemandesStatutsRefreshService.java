package mc.gouv.xaf.back.service.data;

public interface DemandesStatutsRefreshService {

    /**
     * Job Permettant de rafraichir le statut des demandes
     * (à implémenter si besoin dans la démarche cf. CVTCVLC)
     *
     * @return Un message indiquant le succès ou l'échec de l'update.
     */
    String refreshStatuts();

}
