package mc.gouv.xaf.back.paiement.service.purge;

import java.util.List;

public interface PurgePaiementDataService {

    void purgeData(List<String> statuts, int jours);

}
