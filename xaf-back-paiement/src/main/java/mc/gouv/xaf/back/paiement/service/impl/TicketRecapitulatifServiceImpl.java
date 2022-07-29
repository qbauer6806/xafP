package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.paiement.data.entity.MoyenPaiementBO;
import mc.gouv.xaf.back.paiement.data.entity.OperationBO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.TicketRecapitulatifService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TicketRecapitulatifServiceImpl implements TicketRecapitulatifService {

    private static Logger LOGGER = LoggerFactory.getLogger(TicketRecapitulatifServiceImpl.class);

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MailService mailService;

    @Autowired
    private PaiementPropertiesResolver paiementPropertiesResolver;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private GouvBPM gouvBPM;


    public void send(OperationBO operation, MoyenPaiementBO moyenPaiement, Integer demandeId) {

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        Integer usagerId = (Integer) variables.get(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());

        GichuniUsagerDTO usager = usagersCache.get(usagerId, true);

        String bodyTemplateCode = "MAIL_TICKET_RECAPITULATIF_CORPS";
        String subjectTemplateCode = "MAIL_TICKET_RECAPITULATIF_OBJET";

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());
        emailInfo.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        emailInfo.setLangue("fr");


        Map<String, Object> model = getModel(operation, moyenPaiement, usager);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private Map<String, Object> getModel(OperationBO operation, MoyenPaiementBO moyenPaiement, GichuniUsagerDTO usager) {
        Map<String, Object> model = new HashMap<>();
        model.put("demandeurTitre", usager.getPrenom() + " " + usager.getNom());
        model.put("numero_TPE", paiementPropertiesResolver.getTpe());
        model.put("PK_operation", operation.getPkOperation());
        model.put("date", operation.getDateCreation());
        model.put("montant", operation.getMontant());
        model.put("type_carte", moyenPaiement.getBrand());
        model.put("type_transaction", moyenPaiement.getMoyenPaiementType());
        model.put("cbmasquee", moyenPaiement.getBincb());
        model.put("numero_autorisation", operation.getNumeroAuthorisation());
        return model;
    }


}
