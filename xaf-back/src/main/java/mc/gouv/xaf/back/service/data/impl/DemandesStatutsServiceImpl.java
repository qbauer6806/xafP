package mc.gouv.xaf.back.service.data.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.DemandesStatutsRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandesStatutsTransformer;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.data.StatistiquesService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.DemandeRecapDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.utils.GUKafkaUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des statuts des demandes.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesStatutsServiceImpl implements DemandesStatutsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesStatutsServiceImpl.class);

    private final DemandesRepository demandesRepository;
    private final DemandesStatutsRepository demandesStatutsRepository;
    private final StatistiquesService statistiquesService;
    private final DemarchesDataProvider demarchesDataProvider;
    private final GUKafkaUtils guKafkaUtils;
    private final GUKafkaProducer guKafkaProducer;
    private final DemandesTransformer demandesTransformer;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesHelperService demandesHelperService;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO updateStatut(Integer demandeId, String statutName, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {

        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, false);

        // Gérer les accès désactivés
        //#4877 - Traitement après désinscription, Il faut pouvoir mettre à jour des statuts de demande même si l'usager s'est désactivé de la démarche
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        return updateStatut(demandeBo, statutName, agentId, usagerId, codeMotif, commentaire, texteAEnvoyer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeDTO updateStatut(DemandeBO demande, String statutName, String agentId, Integer usagerId,
            String codeMotif, String commentaire, String texteAEnvoyer) {
        try {
            LOGGER.info("updateStatut({}, {}, {}, {}, {}, {}, {})", demande.getPkDemandes(), statutName, agentId,
                    usagerId, codeMotif, commentaire, texteAEnvoyer);

            String statutInitial = null;
            if (demande.getDernierStatut() != null) {
                statutInitial = demande.getDernierStatut().getName();
            }

            LOGGER.info("Constitution du nouveau statut ({}) et sauvegarde en base...", statutName);
            DemandesStatutsBO statutBo = new DemandesStatutsBO();
            statutBo.setLibelle(demarchesDataProvider.getStatusLibelle(statutName));
            statutBo.setName(statutName);
            statutBo.setDate(new Date());
            statutBo.setUsagerId(usagerId);
            statutBo.setAgentId(agentId);
            statutBo.setCodeMotif(codeMotif);
            statutBo.setCommentaire(commentaire);
            statutBo.setTexteAEnvoyer(texteAEnvoyer);
            statutBo.setFkDemandes(demande);
            if (demande.getStatuts() == null) {
                demande.setStatuts(new HashSet<>());
            }
            statutBo = demandesStatutsRepository.save(statutBo);
            demande.getStatuts().add(statutBo);
            demande.setDernierStatut(statutBo);
            demande = demandesRepository.save(demande);

            StatutSimplifieEnum statutSimplifieInitial = demarchesDataProvider.getStatutSimplifie(statutInitial);
            if (statutSimplifieInitial == null) {
                LOGGER.info(
                        "Le statut simplifié initial est null, probablement une création de demande, donc aucun message à envoyer au Guichet Unique via Kafka");
            } else if (statutSimplifieInitial.equals(StatutSimplifieEnum.TERMINEE)) {
                LOGGER.info(
                        "Le statut simplifié initial est TERMINEE, il s'agit donc probablement d'une duplication de demande, donc aucun message à envoyer au Guichet Unique via Kafka");
            } else {
                StatutSimplifieEnum statutSimplifieNouveau = demarchesDataProvider.getStatutSimplifie(statutName);
                if (statutSimplifieInitial.equals(statutSimplifieNouveau)) {
                    LOGGER.info(
                            "Le statut simplifié n'a pas changé, pas d'envoi de message au Guichet Unique via Kafka.");
                } else {
                    LOGGER.info("Le statut simplifié a changé, envoi d'un message au Guichet Unique via Kafka...");
                    List<DemandeRecapDTO> demandeRecaps = guKafkaUtils.getDemandeRecapsFromUsagerId(
                            demande.getFkAccess().getUsagerId());
                    RecapDemandesDTO recapDemandes = guKafkaUtils.getRecapDemandes(demandeRecaps);
                    guKafkaProducer.sendChangementStatutDemandeMessage(demande.getFkAccess().getUsagerId(),
                            demande.getPkDemandes(), demande.getIdentifiant(), statutSimplifieNouveau,
                            statutBo.getDate(), recapDemandes);
                }
            }

            DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demande);
            statistiquesService.saveStatistique(demandeDTO);

            return demandeDTO;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de updateStatut");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesStatutsServiceImpl - méthode updateStatut()", demande.getPkDemandes(), e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateMultipleStatuts(List<DemandeBO> demandes, String statutName) {
        try {
            for (DemandeBO demandeBo : demandes) {
                AccessBO accessBO = demandeBo.getFkAccess();
                accessBO.setUsagerId(demandeBo.getUsager().getId());
                updateStatut(demandeBo, statutName, null, null, null, null, null);
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de updateMultipleStatuts");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesStatutsServiceImpl - méthode updateMultipleStatuts()", demandes, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeStatutDTO> getStatuts(Integer demandeId) {

        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(demandeId, false);

        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        return DemandesStatutsTransformer.bo2Dto(new ArrayList<>(demandeBo.getStatuts()));
    }

}
