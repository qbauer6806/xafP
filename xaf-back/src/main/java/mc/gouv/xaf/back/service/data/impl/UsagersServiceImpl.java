package mc.gouv.xaf.back.service.data.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.UsagersService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;

/**
 * Service permettant de gérer les usagers.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class UsagersServiceImpl implements UsagersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCourrierServiceImpl.class);

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private AccessService accessService;

    @Autowired
    private DemandesStatutsService demandesStatutsService;
    
    @Autowired
    private DemandesRepository demandesRepository;
    
    @Autowired
    private BrouillonsService brouillonsService;

    @Override
    public void desinscriptionUsager(String demarcheId, Integer usagerId,
            List<String> statutsFinaux, String statutAnnulation, String codeMotif) {

        LOGGER.info("Récupération des demandes liées à l'usager...");
        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setDemarcheId(demarcheId);
        demandeRecherche.setUsagerId(usagerId);
        List<DemandeDTO> demandes = demandesService.getDemandes(demandeRecherche);

        // LOGGER.info("Mise à jour du canal des demandes...");
        // for (DemandeDTO demande : demandes) {
        // DemandeDTO newDem = new DemandeDTO();
        // newDem.setPkDemandes(demande.getPkDemandes());
        // newDem.setCanal(DemandeCanalEnum.COURRIER);
        // demandesService.updateDemande(newDem, true);
        // }

        LOGGER.info("Mise à jour du statut des demandes...");
        for (DemandeDTO demande : demandes) {
            boolean isFinal = false;
            for (String statut : statutsFinaux) {
                if (statut.equals(demande.getDernierStatut().getLibelle())) {
                    isFinal = true;
                }
            }
            if (!isFinal && !statutAnnulation.equals(demande.getDernierStatut().getLibelle())) {
                demandesStatutsService.updateStatut(demande.getDemarcheId(), demande.getPkDemandes(), statutAnnulation,
                        null, usagerId, codeMotif, null, null);
            }
        }
        
        LOGGER.info("Suppression des brouillons...");
        brouillonsService.deleteBrouillons(demarcheId, usagerId);

        LOGGER.info("Suppression de l'accès...");
        accessService.deleteAccess(demarcheId, usagerId);

    }
    
    @Override
    public Integer getNbDemandesUsager(String demarcheId, Integer usagerId) {
    	return demandesRepository.getNbDemandesForUsager(demarcheId,
    			usagerId);
    }

}
