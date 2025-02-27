package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.UsagersService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeRechercheDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service permettant de gérer les usagers.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class UsagersServiceImpl implements UsagersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersServiceImpl.class);

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

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Override
    public void desinscriptionUsager(Integer usagerId, String statutAnnulation, String codeMotif) {

        LOGGER.info("Récupération des demandes liées à l'usager...");
        DemandeRechercheDTO demandeRecherche = new DemandeRechercheDTO();
        demandeRecherche.setUsagerId(usagerId);
        List<DemandeDTO> demandes = demandesService.getDemandes(demandeRecherche);

        desinscriptionUsager(usagerId, statutAnnulation, codeMotif, demandes);
    }

    @Override
    public void desinscriptionUsager(Integer usagerId, String statutAnnulation, String codeMotif,
            List<DemandeDTO> demandesAPasserEnAnnuleeDTO) {

        LOGGER.info("Mise à jour du statut des demandes...");
        for (DemandeDTO demande : demandesAPasserEnAnnuleeDTO) {
            boolean isFinal = demarchesDataProvider.getStatutSimplifie(demande.getDernierStatut().getName())
                    .equals(StatutSimplifieEnum.TERMINEE);
            if (!isFinal && !statutAnnulation.equals(demande.getDernierStatut().getName())) {
                demandesStatutsService.updateStatut(demande.getPkDemandes(), statutAnnulation, null, usagerId,
                        codeMotif, null, null);
            }
        }

        LOGGER.info("Suppression des brouillons...");
        brouillonsService.deleteBrouillons(usagerId);

        LOGGER.info("Suppression de l'accès...");
        accessService.deleteAccess(usagerId);
    }

    @Override
    public Integer getNbDemandesUsager(Integer usagerId) {
        return demandesRepository.countByFkAccess_UsagerIdAndFkAccess_ActiveTrue(usagerId);
    }

}
