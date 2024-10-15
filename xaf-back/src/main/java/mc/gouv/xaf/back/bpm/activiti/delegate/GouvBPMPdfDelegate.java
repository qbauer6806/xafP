package mc.gouv.xaf.back.bpm.activiti.delegate;

import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour générer un courrier PDF.
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMPdfDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMPdfDelegate.class);

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private DemandesService demandesService;

    @Getter
    @Setter
    private Expression pdfTypeCodeExpr;

    private Expression meta;

    @Override
    public void execute(DelegateExecution execution) {

        LOGGER.info("==== xaf-back PDF SERVICE ...");

        DemandeDTO demandeDto = demandesService.getDemande(Integer.parseInt(execution.getProcessInstanceBusinessKey()));
        
        PdfTypeEnum pdfType;
        if (pdfTypeCodeExpr == null) {
        	pdfType = PdfTypeEnum.COURRIER;
        }
        else {
        	String pdfTypeCodeStr = (String) pdfTypeCodeExpr.getValue(execution);
        	pdfType = PdfTypeEnum.valueOf(pdfTypeCodeStr);
        }
        
        String metaStr = null;
        if (meta != null) {
        	metaStr = (String)meta.getValue(execution);
        }

        try {
            pdfGenerationService.generateAndStorePdf(demandeDto, pdfType, metaStr);
        } catch (IOException e) {
            throw new DemarcheException("Erreur la génération du pdf", e);
        }

        LOGGER.info("==== xaf-back PDF SERVICE <fin>");
    }

}
