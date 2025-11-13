package mc.gouv.xaf.back.service.data.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service permettant la manipulation des démarches.
 *
 * @author qdeme
 */
@Service
@RequiredArgsConstructor
public class DemandesHelperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesHelperService.class);

    private final DemandesRepository demandesRepository;

    public DemandeBO getCheckDemarcheDemandeBO(Integer demandeId, boolean checkActive) {
        LOGGER.debug("Récupération en base des demandes...");
        Optional<DemandeBO> demandeBoOp = demandesRepository.findById(demandeId);

        // Gérer les accès désactivés
        if (demandeBoOp.isPresent() && !demandeBoOp.get().getFkAccess().isActive() && DemarchesUtils.isFrontUser()
                && checkActive) {
            demandeBoOp = Optional.empty();
        }

        if (demandeBoOp.isEmpty()) {
            LOGGER.error("Le demande ID: {}, est introuvable.", demandeId);
            throw new DemarchesServiceException("Demande introuvable ou supprimée", HttpStatus.NOT_FOUND);
        }

        return demandeBoOp.get();
    }
}
