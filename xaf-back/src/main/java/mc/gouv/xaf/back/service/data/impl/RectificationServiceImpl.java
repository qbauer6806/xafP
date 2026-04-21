package mc.gouv.xaf.back.service.data.impl;

import static mc.gouv.xaf.back.service.AfApiService.AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.bpm.activiti.exception.TaskAlreadyClaimedException;
import mc.gouv.xaf.back.bpm.model.GouvBPMTask;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.RectificationService;
import mc.gouv.xaf.back.service.demande.UpdateDemandeExtender;
import mc.gouv.xaf.back.service.demande.UpdateDemandeFinalizer;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.back.service.utils.RelancesUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.enums.XafDemandeStatutEnum;
import mc.gouv.xaf.shared.exception.DemarcheException;
import mc.gouv.xapi.error.exception.client.BadRequestWebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class RectificationServiceImpl implements RectificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RectificationServiceImpl.class);

    private final GouvBPM gouvBPM;
    private final DemandesService demandesService;
    private final DemandesDataService demandesDataService;
    private final DemandesHistoriqueService demandesHistoriqueService;
    private final Optional<UpdateDemandeFinalizer> updateDemandeFinalizers;
    private final Optional<UpdateDemandeExtender> updateDemandeExtenders;

    @Override
    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId, String agentId) {

        DemandeDTO demandeEnBase = demandesService.getDemande(demandeId);
        if (!XafDemandeStatutEnum.EN_ATTENTE_RECTIFICATION.name().equals(demandeEnBase.getDernierStatut().getName())) {
            throw new BadRequestWebException("La demande n'est pas éligible à une rectification.");
        }
        try {
            if (demande != null) {
                // Cas rectification par usager
                final DemandeDTO demandeDto = buildDemandeFromInputAfterUpdate(demande, usagerId, demandeId);

                updateDemandeExtenders.ifPresent(extender -> extender.applyUpdateTreatment(demande, demandeDto));

                LOGGER.debug("DTO reconstitué : {}", demandeDto);

                // Partial update sur contenu et fichiers uniquement

                DemandeDTO demandeDtoUpdated = demandesService.updateDemande(demandeDto, true);

                LOGGER.debug("DTO après sauvegarde en base : {}", demandeDtoUpdated);

                updateDemandeFinalizers.ifPresent(finalizer -> finalizer.finalizeDemandeUpdate(demandeDtoUpdated));
            }

            GouvBPMUser user = new GouvBPMUser();
            if (usagerId != null) {
                user.setId(usagerId.toString());
            } else if (agentId != null) {
                user.setId(agentId);
            }

            // Définition de variables process à destination du GouvBPMStatusChangeService pour qu'il puisse savoir qui est
            // à l'origine du changement de statut qui va suivre
            LOGGER.info("Progression dans le BPM...");
            Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
            if (usagerId != null) {
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(),
                        usagerId.toString());
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), null);
            } else if (agentId != null) {
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_AGENT.name(), agentId);
                variables.put(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name(), null);
            }
            gouvBPM.setProcessBusinessVariables(demandeId, variables);

            GouvBPMTask task = gouvBPM.getActiveTasksForDemande(demandeId).getFirst();

            try {
                gouvBPM.claimTask(task, user);
            } catch (TaskAlreadyClaimedException e1) {
                throw new DemarcheException("Erreur lors du claim de la tache", e1);
            }
            gouvBPM.completeTask(task, demandeId);

            // Ajout d'une ligne à l'historique
            LOGGER.info(AJOUT_LIGNE_HISTORIQUE_LOG_MESSAGE);
            // on est obligé de rafraichir la demande afin de récupérer le nouveau statut qui a tout juste changé grâce au bpmn
            demandeEnBase = demandesService.getDemande(demandeId);
            DemandeHistoriqueDTO histo = demandesHistoriqueService.updateDemande(
                    demandeEnBase.getDernierStatut().getName(), usagerId, agentId);
            demandesHistoriqueService.saveHisto(demandeId, histo);

            demandesDataService.deleteDemandeData(demandeId, RelancesUtils.DATES_RELANCES_KEY);

            return demandeEnBase;
        } catch (Exception e) {
            // Renvoi d'une exception pour que l'utilisateur sache qu'il y a eu une erreur
            throw new DemarcheException("Erreur lors de la mise à jour d'une demande", e);
        }
    }

    private DemandeDTO buildDemandeFromInputAfterUpdate(DemandeInputDTO demande, Integer usagerId, Integer demandeId) {
        DemandeDTO demandeDto = new DemandeDTO();
        demandeDto.setUsagerId(usagerId);
        demandeDto.setPkDemandes(demandeId);
        demandeDto.setContenu(demande.getContenu());
        demandeDto.setFichiers(demande.getFichiers());
        return demandeDto;
    }
}
