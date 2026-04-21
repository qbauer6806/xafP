package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.recap.PdfRecapGenerationService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour générer le fichier interne de la récapitulation d'une demande au
 * format PDF.
 *
 * @author mboutelier.ext
 */
@Component
@RequiredArgsConstructor
public class GouvBPMDemandeRecapDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeRecapDelegate.class);

    private final DemandesService demandesService;
    private final PdfRecapGenerationService pdfRecapGenerationService;

    private Expression overwrite;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back DEMANDE RECAP SERVICE ...");

        String overwriteStr = null;
        if (overwrite != null) {
            overwriteStr = ((String) overwrite.getValue(execution));
        }

        DemandeDTO demandeDto = demandesService.getDemande(Integer.parseInt(execution.getProcessInstanceBusinessKey()));

        try {
            pdfRecapGenerationService.generateAndStorePdf(demandeDto,
                    overwriteStr == null || Boolean.parseBoolean(overwriteStr));
        } catch (IOException e) {
            throw new DemarcheException("Erreur la génération du pdf", e);
        }

        LOGGER.info("==== xaf-back DEMANDE RECAP SERVICE <fin>");
    }

}
