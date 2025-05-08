package mc.gouv.xaf.back.paiement.service.data;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;

public interface MoyenPaiementService {

    MoyenPaiementDTO findByFkDemandesAndLatestCommande(Integer fkDemandes);

}
