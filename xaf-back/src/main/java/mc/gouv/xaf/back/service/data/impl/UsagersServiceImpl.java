package mc.gouv.xaf.back.service.data.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.UsagersService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant de gérer les usagers.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UsagersServiceImpl implements UsagersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersServiceImpl.class);

    private final AccessService accessService;
    private final DemandesStatutsService demandesStatutsService;
    private final DemandesRepository demandesRepository;
    private final BrouillonsService brouillonsService;

    @Override
    public void desinscriptionUsager(Integer usagerId, String statutAnnulation, String codeMotif,
                                     List<DemandeDTO> demandesAPasserEnAnnuleeDTO) {

        LOGGER.info("Mise à jour du statut des demandes...");
        for (DemandeDTO demande : demandesAPasserEnAnnuleeDTO) {
            //Si la demande n'a pas le même statut déjà.
            if (!StringUtils.equals(statutAnnulation, demande.getDernierStatut().getName())) {
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
