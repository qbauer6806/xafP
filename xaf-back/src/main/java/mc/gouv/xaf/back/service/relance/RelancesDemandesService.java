package mc.gouv.xaf.back.service.relance;

import java.util.Map;

import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface RelancesDemandesService {

	public void sendRelancesMail(Map<String, String> statutsARelancer);

	public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix);

}
