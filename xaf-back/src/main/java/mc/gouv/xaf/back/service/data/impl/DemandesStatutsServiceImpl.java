package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.transformer.DemandesStatutsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.back.shared.dto.DemandeDTO;
import mc.gouv.xaf.back.shared.dto.DemandeStatutDTO;

/**
 * Service permettant la manipulation des statuts des demandes.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesStatutsServiceImpl implements DemandesStatutsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesStatutsServiceImpl.class);

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesStatutsRepository demandesStatutsRepository;
    
    @Autowired
    private DemandesService demandesService;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO updateStatut(String demarcheId, Integer demandeId, String statut, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        // Gérer les accès désactivés
        //#4877 - Traitement après désinscription, Il faut pouvoir mettre à jour des statuts de demande même si l'usager s'est désactivé de la démarche

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        demandeBo = updateStatut(demandeBo, statut, agentId, usagerId, codeMotif, commentaire, texteAEnvoyer);
        return DemandesTransformer.bo2Dto(demandeBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeBO updateStatut(DemandeBO demande, String statut, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {

        LOGGER.info("Constitution du nouveau statut (" + statut + ") et sauvegarde en base...");
        DemandeStatutDTO statutDto = new DemandeStatutDTO();
        statutDto.setLibelle(statut);
        statutDto.setDate(new Date());
        statutDto.setUsagerId(usagerId);
        statutDto.setAgentId(agentId);
        statutDto.setCodeMotif(codeMotif);
        statutDto.setCommentaire(commentaire);
        statutDto.setTexteAEnvoyer(texteAEnvoyer);
        DemandesStatutsBO statutBo = DemandesStatutsTransformer.dto2Bo(statutDto);
        statutBo.setFkDemandes(demande);
        if (demande.getStatuts() == null) {
            demande.setStatuts(new HashSet<DemandesStatutsBO>());
        }
//        demande.getStatuts().add(statutBo);
//        demandesStatutsRepository.save(statutBo);
//        demande.setDernierStatut(statutBo);
//        demande = demandesRepository.save(demande);
        statutBo = demandesStatutsRepository.save(statutBo);
        demande.getStatuts().add(statutBo);
        demande.setDernierStatut(statutBo);
        demande = demandesRepository.save(demande);

        return demande;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeStatutDTO getStatut(String demarcheId, Integer demandeId) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        // Gérer les accès désactivés

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        DemandesStatutsBO statut = DemarchesUtils.getLatestStatus(demandeBo);

        return DemandesStatutsTransformer.bo2Dto(statut);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeStatutDTO> getStatuts(String demarcheId, Integer demandeId) {

        LOGGER.info("Récupération en base de la demande...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, demandeId, false);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande introuvable", HttpStatus.NOT_FOUND);
        }

        return DemandesStatutsTransformer.bo2Dto(new ArrayList<DemandesStatutsBO>(demandeBo.getStatuts()));
    }

}
