package mc.gouv.xaf.xaf12batch;

import mc.gouv.xaf.xaf12batch.marqueurs.MarqueursService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResetMarqueursTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetMarqueursTasklet.class);

    @Autowired
    private MarqueursService marqueursService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LOGGER.info("Début de la génération des marqueurs");
        marqueursService.resetMarqueurs();
        LOGGER.info("Fin de la génération des marqueurs");
        return RepeatStatus.FINISHED;
    }
}
