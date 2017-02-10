package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.pdf.PdfService;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeDTO;

@Component
public class GouvBPMPdfDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMPdfDelegate.class);
    
    @Autowired
    private PdfService pdfService;
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    private DemClient demClient;
    
    @Override
    public void execute(DelegateExecution execution) throws Exception {

        LOGGER.info("==== AF-BACK PDF SERVICE ...");
        
        DemandeDTO demandeDto = getDemClient().getDemande(afBackUtils.getDemarcheId(), Integer.parseInt(execution.getProcessBusinessKey()));
        
        pdfService.generatePdf(demandeDto);
        
        LOGGER.info("==== AF-BACK PDF SERVICE <fin>");
    }
    
    private DemClient getDemClient() {
        if (demClient == null) {
            demClient = new DemClient(afBackUtils.getDemUrl(), afBackUtils.getDemUser(), afBackUtils.getDemPwd());
        }
        return demClient;
    }

}
