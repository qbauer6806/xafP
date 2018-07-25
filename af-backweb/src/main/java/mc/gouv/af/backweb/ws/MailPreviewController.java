package mc.gouv.af.backweb.ws;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.af.back.bpm.GouvBPM;
import mc.gouv.af.back.mail.MailService;
import mc.gouv.af.back.mail.MailTemplateModelProvider;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.backweb.controller.AbstractController;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Sert à générer la preview des emails
 * 
 * @author qdeme
 *
 */
@Controller
@RequestMapping("/ws/mailpreview")
public class MailPreviewController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPreviewController.class);

    @Autowired
    private MailService mailService;
    
    @Autowired
    private MailTemplateModelProvider mailTemplateModelProvider;
    
    @Autowired
    private DemandesService demandesService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @Autowired
    private GouvBPM gouvBPM;

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView mailpreview(@RequestParam(required = true) String action,
            @RequestParam(required = true) String codeMotifChoisi, @RequestParam(required = true) Integer pkDemande,
            @RequestParam(required = true) String commentaire) throws Exception {

        LOGGER.info("======================= Appel de /ws/mailpreview (" + action + "," + codeMotifChoisi + ","
                + pkDemande + "," + commentaire + ")");

        Entry<String, String> templateCodes = mailTemplateModelProvider.getMailTemplateCodesForAction(action);
        String bodyTemplateCode = templateCodes.getKey();
        String subjectTemplateCode = templateCodes.getValue();
        
        Map<String, Object> bpmVariables = gouvBPM.getProcessBusinessVariables(pkDemande);
        
        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), pkDemande);

        Map<String, Object> model = mailTemplateModelProvider.getModel(subjectTemplateCode, bodyTemplateCode, demande, bpmVariables, codeMotifChoisi, commentaire);

        LOGGER.info("Génération de l'aperçu de l'email...");
        String[] preview = mailService.getMailPreview(bodyTemplateCode, subjectTemplateCode, demande.getLangue(), model);

        ModelAndView mav = new ModelAndView("misc/mailpreview");
        mav.addObject("mailSubject", preview[0]);
        mav.addObject("mailBody", preview[1]);

        LOGGER.info("======================= Fin /ws/mailpreview");

        return mav;

    }

}
