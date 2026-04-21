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
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe service appelée par le process Activiti pour générer un courrier PDF.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GouvBPMPdfDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMPdfDelegate.class);

    private final PdfGenerationService pdfGenerationService;
    private final DemandesService demandesService;
    private final AfPdfTemplateAndModelProvider afPdfTemplateAndModelProvider;

    private Expression pdfTypeCodeExpr;
    private Expression meta;
    private Expression template;
    private Expression filename;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back PDF SERVICE ...");

        try {
            DemandeDTO demandeDto = demandesService.getDemande(
                    Integer.parseInt(execution.getProcessInstanceBusinessKey()));

            PdfTypeEnum pdfType = this.getPdfTypeEnum(execution);

            String metaStr = this.getMetaStr(execution);

            String templateStr = this.getTemplateStr(execution);

            String filenameStr = this.getFilenameStr(execution);
            PdfTemplateAndModelDTO pdfTemplateAndModelDTO = afPdfTemplateAndModelProvider.getTemplateAndModel(
                    demandeDto, pdfType);
            // on surcharge le template et le filename par celui du bpmn si spécifié
            if (StringUtils.isNotBlank(templateStr)) {
                pdfTemplateAndModelDTO.setTemplateFilename(templateStr);
            }
            if (StringUtils.isNotBlank(filenameStr)) {
                pdfTemplateAndModelDTO.setFilename(filenameStr + "_" + demandeDto.getIdentifiant() + "_");
            }
            pdfGenerationService.generateAndStoreDoc(demandeDto, pdfType, metaStr, pdfTemplateAndModelDTO, true);
        } catch (IOException e) {
            throw new DemarcheException("Erreur la génération du pdf", e);
        } finally {
            // On est obligés de réinitialiser les champs car si ils ne sont pas spécifiés sur une prochaine execution les valeurs vont rester
            pdfTypeCodeExpr = null;
            meta = null;
            template = null;
            filename = null;
        }

        LOGGER.info("==== xaf-back PDF SERVICE <fin>");
    }

    private String getMetaStr(DelegateExecution execution) {
        String metaStr = null;
        if (meta != null) {
            metaStr = (String) meta.getValue(execution);
        }
        return metaStr;
    }

    private PdfTypeEnum getPdfTypeEnum(DelegateExecution execution) {
        PdfTypeEnum pdfType;
        if (pdfTypeCodeExpr == null) {
            pdfType = PdfTypeEnum.COURRIER;
        } else {
            String pdfTypeCodeStr = (String) pdfTypeCodeExpr.getValue(execution);
            pdfType = PdfTypeEnum.valueOf(pdfTypeCodeStr);
        }
        return pdfType;
    }

    private String getFilenameStr(DelegateExecution execution) {
        String filenameStr = null;
        if (filename != null) {
            filenameStr = (String) filename.getValue(execution);
        }
        return filenameStr;
    }

    private String getTemplateStr(DelegateExecution execution) {
        String templateStr = null;
        if (template != null) {
            templateStr = (String) template.getValue(execution);
        }
        return templateStr;
    }

}
