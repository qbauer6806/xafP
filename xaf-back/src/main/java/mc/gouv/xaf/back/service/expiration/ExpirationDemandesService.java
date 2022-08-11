package mc.gouv.xaf.back.service.expiration;

import java.util.Map;

import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface ExpirationDemandesService {

	public Map<DemandeDTO, String> getDemandesAExpirer(Map<String, String> statusAExpirer);
	public void expirerDemande(DemandeDTO demandeAExpirer, String mailKeyAEnvoyer);
	public void envoiEmailUsagerExpiration(DemandeDTO demande, String mailKey);
}
