package mc.gouv.xaf.back.service.relance;

import java.util.List;

import mc.gouv.xaf.back.service.relance.settings.RelanceDemandeSettings;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface RelancesDemandesService {

	public void sendRelancesMail(List<RelanceDemandeSettings> statutsARelancer);

	public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix);

}
