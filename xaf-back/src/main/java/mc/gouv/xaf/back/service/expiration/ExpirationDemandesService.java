package mc.gouv.xaf.back.service.expiration;

import mc.gouv.xaf.back.service.expiration.settings.ExpirationStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

import java.util.List;
import java.util.Map;

public interface ExpirationDemandesService {

    Map<DemandeDTO, String> getDemandesAExpirer(List<ExpirationStatutDemandeConf> statusAExpirer);

    /**
     * Permet de passer la demande en paramètre à l'état Expirée
     *
     * @param demandeAExpirer,
     *         la demande à expirer
     * @param mailKeyAEnvoyer,
     *         la clé de l'email à envoyer
     * @param motifs,
     *         la liste des motifs pour l'état Expirée
     */
    void expirerDemande(DemandeDTO demandeAExpirer, String mailKeyAEnvoyer, Map<String, MotifDTO> motifs);

    void envoiEmailUsagerExpiration(DemandeDTO demande, String mailKey);

    void envoiEmailAgentExpiration(DemandeDTO demande, String mailKey);
}
