package mc.gouv.xaf.back.paiement.service.data.impl;

import mc.gouv.xaf.back.paiement.data.dao.MoyenPaiementRepository;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.transformer.MoyenPaiementTransformer;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.service.data.MoyenPaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoyenPaiementServiceImpl implements MoyenPaiementService {

    @Autowired
    private MoyenPaiementRepository moyenPaiementRepository;

    @Override
    public MoyenPaiementDTO findByPkCommande(Integer pkCommande) {
        MoyenPaiementBO bo = moyenPaiementRepository.findByCommande_PkCommandes(pkCommande);
        return MoyenPaiementTransformer.bo2Dto(bo);
    }
}
