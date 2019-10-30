package mc.gouv.xaf.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
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
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandesService demandesService;

    private Expression pdfTypeCodeExpr;
    
    private Expression meta;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== xaf-back PDF SERVICE ...");

        DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                Integer.parseInt(execution.getProcessBusinessKey()));
        
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

        pdfGenerationService.generateAndStorePdf(demandeDto, pdfType, metaStr);

        LOGGER.info("==== xaf-back PDF SERVICE <fin>");
    }

    public Expression getPdfTypeCodeExpr() {
        return pdfTypeCodeExpr;
    }

    public void setPdfTypeCodeExpr(Expression pdfTypeCodeExpr) {
        this.pdfTypeCodeExpr = pdfTypeCodeExpr;
    }
}
