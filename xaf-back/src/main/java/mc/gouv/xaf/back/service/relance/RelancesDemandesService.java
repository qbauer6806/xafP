package mc.gouv.xaf.back.service.relance;

import java.util.List;

import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface RelancesDemandesService {

	void sendRelancesMail(List<RelanceStatutDemandeConf> statutsARelancer);

	void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix);

}
