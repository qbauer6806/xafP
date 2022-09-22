package mc.gouv.xaf.back.service.expiration;

import mc.gouv.xaf.back.service.expiration.settings.ExpirationStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.List;
import java.util.Map;

public interface ExpirationDemandesService {

    Map<DemandeDTO, String> getDemandesAExpirer(List<ExpirationStatutDemandeConf> statusAExpirer);

    void expirerDemande(DemandeDTO demandeAExpirer, String mailKeyAEnvoyer);

    void envoiEmailUsagerExpiration(DemandeDTO demande, String mailKey);

    void envoiEmailAgentExpiration(DemandeDTO demande, String mailKey);
}
