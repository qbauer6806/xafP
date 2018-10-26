package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.service.DemandesComplementsService;
import mc.gouv.dem.shared.model.DemandeComplementsQuestionDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour créer une demande d'informations complémentaires.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMDemandeInfoComplDelegate implements JavaDelegate {

    // voir pour l'autowiring dans les javaDelegate

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeInfoComplDelegate.class);

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesComplementsService demandesComplementsService;

    @Autowired(required = false)
    private IndexedDemandeService indexedDemandeService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK CREATION INFO COMPL ...");

        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        Integer demandeId = Integer.parseInt(execution.getProcessBusinessKey());

        LOGGER.info("Demande : " + demandeId);

        // Récupération du commentaire usager et du code motif si besoin plus tars dans le traitement
        String commentaireUsager = (String) execution.getVariables()
                .get(GouvBPMProcessVariableTypeEnum.MC_COMMENTAIRE_USAGER.name());
        String codeMotif = (String) execution.getVariables().get(GouvBPMProcessVariableTypeEnum.MC_CODE_MOTIF.name());

        LOGGER.info("Commentaire usager : " + commentaireUsager);
        LOGGER.info("Code motif : " + codeMotif);

        DemandeComplementsQuestionDTO questionDto = new DemandeComplementsQuestionDTO();
        questionDto.setAgentId(AfBackUtils.getAuthenticatedAgentId());
        questionDto.setCodeMotif(codeMotif);
        if (!StringUtils.isBlank(commentaireUsager)) {
            questionDto.setTexte(commentaireUsager);
        } else {
            // Texte vide si commentaireUsager null
            questionDto.setTexte("");
        }

        LOGGER.info("Appel à DEM createDemandeComplements()...");
        demandesComplementsService.saveDemandeComplements(demarcheId, demandeId, questionDto);

        if (indexedDemandeService != null) {
            indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demandeId);
        }
        LOGGER.info("==== AF-BACK CREATION INFO COMPL <fin>");

    }
}
