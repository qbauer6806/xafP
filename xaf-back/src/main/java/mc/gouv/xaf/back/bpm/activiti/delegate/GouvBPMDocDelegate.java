package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.pdf.impl.AfPdfTemplateAndModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour générer un doc.
 */
@Component
@RequiredArgsConstructor
public class GouvBPMDocDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDocDelegate.class);

    private final PdfGenerationService pdfGenerationService;
    private final DemandesService demandesService;
    private final AfPdfTemplateAndModelProvider afPdfTemplateAndModelProvider;

    private Expression meta;

    private Expression template;

    private Expression filename;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back DOC SERVICE ...");

        DemandeDTO demandeDto = demandesService.getDemande(Integer.parseInt(execution.getProcessInstanceBusinessKey()));

        String metaStr = null;
        if (meta != null) {
            metaStr = (String) meta.getValue(execution);
        }

        String templateStr = null;
        if (template != null) {
            templateStr = (String) template.getValue(execution);
        }

        String filenameStr = null;
        if (filename != null) {
            filenameStr = (String) filename.getValue(execution);
        }

        PdfTemplateAndModelDTO pdfTemplateAndModelDTO = afPdfTemplateAndModelProvider.getTemplateAndModel(demandeDto,
                PdfTypeEnum.FICHIER);
        // on surcharge le template et le filename par celui du bpmn si spécifié
        if (templateStr != null && !templateStr.isEmpty()) {
            pdfTemplateAndModelDTO.setTemplateFilename(templateStr);
            pdfTemplateAndModelDTO.setFilename(filenameStr + "_" + demandeDto.getIdentifiant() + "_");
        }

        try {
            pdfGenerationService.generateAndStoreDoc(demandeDto, PdfTypeEnum.FICHIER, metaStr, pdfTemplateAndModelDTO);
        } catch (IOException e) {
            throw new DemarcheException("Erreur la génération du doc", e);
        } finally {
            // On est obligés de réinitialiser les champs car si ils ne sont pas spécifiés sur une prochaine execution les valeurs vont rester
            meta = null;
            template = null;
            filename = null;
        }

        LOGGER.info("==== xaf-back DOC SERVICE <fin>");
    }

}
