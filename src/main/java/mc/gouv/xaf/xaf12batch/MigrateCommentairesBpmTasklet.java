package mc.gouv.xaf.xaf12batch;

import java.util.List;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.xaf12batch.bpm.DemandesCommentaireBO;
import mc.gouv.xaf.xaf12batch.bpm.DemandesCommentaireRepository;
import mc.gouv.xaf.xaf12batch.demandes.DemandesRepository;
import mc.gouv.xaf.xaf12batch.dto.DemandeBO;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MigrateCommentairesBpmTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateCommentairesBpmTasklet.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesCommentaireRepository demandesCommentaireRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LOGGER.info("Début de la migration des commentaires BPM");
        List<Task> tasks = taskService.createTaskQuery().active().list();
        for (Task t : tasks) {
            Integer demandeId = Integer.parseInt(
                    runtimeService.createProcessInstanceQuery().processInstanceId(t.getProcessInstanceId())
                            .singleResult().getBusinessKey());
            if (demandesRepository.existsById(demandeId)) {
                LOGGER.info("Récupération des commentaires liés à la demande {} ...", demandeId);
                List<CommentaireInterneDTO> commInternes;
                commInternes = (List<CommentaireInterneDTO>) runtimeService.getVariable(t.getProcessInstanceId(),
                        "MC_COMMINTERNES");
                if (commInternes != null && !commInternes.isEmpty()) {
                    DemandeBO demandeBO = new DemandeBO();
                    demandeBO.setPkDemandes(demandeId);
                    // on migre les données
                    for (CommentaireInterneDTO commInterne : commInternes) {
                        DemandesCommentaireBO demandesCommentaireBO = new DemandesCommentaireBO();
                        demandesCommentaireBO.setCommentaire(commInterne.getCommentaire());
                        demandesCommentaireBO.setAgentId(commInterne.getAgentId());
                        demandesCommentaireBO.setDate(commInterne.getDate());
                        demandesCommentaireBO.setFkDemandes(demandeBO);
                        DemandesCommentaireBO savedCommentaire = demandesCommentaireRepository.save(
                                demandesCommentaireBO);
                        LOGGER.info("Commentaire ID {} sauvegardé en base",
                                savedCommentaire.getPkDemandesCommentaire());
                    }
                }
            }
            runtimeService.removeVariable(t.getProcessInstanceId(), "MC_COMMINTERNES");
        }
        LOGGER.info("Fin de la migration des commentaires BPM");
        return RepeatStatus.FINISHED;
    }

}
