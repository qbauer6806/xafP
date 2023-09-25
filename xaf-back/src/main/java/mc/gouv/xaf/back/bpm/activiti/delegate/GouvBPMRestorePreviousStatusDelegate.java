package mc.gouv.xaf.back.bpm.activiti.delegate;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesStatutsService;
import mc.gouv.xaf.back.service.utils.DemandeStatutComparator;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Classe service appelée par le process Activiti afin de remettre le statut d'avant (dans une
 * nouvelle instance de statut évidemment).
 *
 * @author qdeme
 */
@Component
public class GouvBPMRestorePreviousStatusDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMRestorePreviousStatusDelegate.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesStatutsService demandesStatutsService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== xaf-back RESTORE PREVIOUS STATUS DELEGATE ...");

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : {}", demandeId);

        // Récupération du commentaire usager, du texte à envoyer et du code motif si besoin plus tard dans le traitement
        String commentaireUsager = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String texteAEnvoyer = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TEXTE_A_ENVOYER.name());

        String codeMotifStr = (String) execution.getVariable(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

        String usagerId = (String) execution
                .getVariable(GouvBPMProcessVariableTypeEnum.MC_TARGETSTATE_ORIGINATOR_USAGER.name());
        if (null == usagerId) {
            throw new DemarchesServiceException("UsagerID null !", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("Commentaire usager : {}", commentaireUsager);
        LOGGER.info("Texte à envoyer : {}", texteAEnvoyer);
        LOGGER.info("Code motif : {}", codeMotifStr);

        LOGGER.info("Récupération du statut précédent...");
        List<DemandeStatutDTO> statuts = demandesStatutsService.getStatuts(gouvPropertiesResolver.getDemarcheId(), demandeId);
        statuts.sort(new DemandeStatutComparator());
        // Récupération du statut avant le statut courant
        DemandeStatutDTO statut = statuts.get(statuts.size() - 2);

        LOGGER.info("Statut à créer : {}", statut.getLibelle());

        LOGGER.info("Appel à demandesStatutsService.updateStatut()...");

        demandesStatutsService.updateStatut(gouvPropertiesResolver.getDemarcheId(), demandeId, statut.getLibelle(), null,
                Integer.parseInt(usagerId), codeMotifStr, commentaireUsager, texteAEnvoyer);

        LOGGER.info("==== xaf-back RESTORE PREVIOUS STATUS DELEGATE <fin>");
    }

}
