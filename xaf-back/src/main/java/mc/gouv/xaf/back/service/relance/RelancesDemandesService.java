package mc.gouv.xaf.back.service.relance;

import java.util.List;

import mc.gouv.xaf.back.service.relance.settings.RelanceStatutDemandeConf;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface RelancesDemandesService {

	public void sendRelancesMail(List<RelanceStatutDemandeConf> statutsARelancer);

	public void envoiEmailUsagerRelance(DemandeDTO demande, String codeMailPrefix);

}
