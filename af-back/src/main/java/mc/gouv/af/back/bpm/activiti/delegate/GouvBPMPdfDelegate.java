package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.pdf.PdfService;
import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeDTO;

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
    private PdfService pdfService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private DemClient demClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK PDF SERVICE ...");

        DemandeDTO demandeDto = getDemClient().getDemande(gouvPropertiesResolver.getDemarcheId(),
                Integer.parseInt(execution.getProcessBusinessKey()));

        pdfService.generatePdf(demandeDto);

        LOGGER.info("==== AF-BACK PDF SERVICE <fin>");
    }

    private DemClient getDemClient() {
        if (demClient == null) {
            demClient = new DemClient(gouvPropertiesResolver.getDemUrl(), gouvPropertiesResolver.getDemUser(),
                    gouvPropertiesResolver.getDemPwd());
        }
        return demClient;
    }

}
