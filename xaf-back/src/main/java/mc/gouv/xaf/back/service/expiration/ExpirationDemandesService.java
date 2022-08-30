package mc.gouv.xaf.back.service.expiration;

import java.util.List;
import java.util.Map;

import mc.gouv.xaf.back.service.expiration.settings.ExpirationDemandeSettings;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface ExpirationDemandesService {

	public Map<DemandeDTO, String> getDemandesAExpirer(List<ExpirationDemandeSettings> statusAExpirer);
	public void expirerDemande(DemandeDTO demandeAExpirer, String mailKeyAEnvoyer);
	public void envoiEmailUsagerExpiration(DemandeDTO demande, String mailKey);
}
