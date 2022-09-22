package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.TicketRecapitulatifService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UsagersUtils;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class TicketRecapitulatifServiceImpl implements TicketRecapitulatifService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketRecapitulatifServiceImpl.class);

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

    @Override
    public void sendMail(CommandeOperationDTO operation, CommandeDTO commandeDTO, Integer demandeId) {

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        Integer usagerId = (Integer) variables.get(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());

        GichuniUsagerDTO usager = usagersCache.get(usagerId, true);

        String bodyTemplateCode = "MAIL_TICKET_RECAP_USAGER_CORPS";
        String subjectTemplateCode = "MAIL_TICKET_RECAP_USAGER_OBJET";

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos()
                .getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos()
                .getEmailReplytoNom());
        emailInfo.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        emailInfo.setLangue("fr");


        try {
            Map<String, Object> model = getModel(operation, commandeDTO.getMoyenPaiement(), usager);
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private Map<String, Object> getModel(CommandeOperationDTO operation, MoyenPaiementDTO moyenPaiement, GichuniUsagerDTO usager) {
        Map<String, Object> model = new HashMap<>();
        model.put("titre", UsagersUtils.titreShortToString(usager.getTitre()));
        model.put("numTPE", paiementPropertiesResolver.getTpe());
        model.put("pkOperation", operation.getPkOperations());
        model.put("dateTransaction", operation.getDateCreation().format(DateTimeFormatter.ofPattern(AfBackUtils.DEFAULT_FRENCH_DATE_HOURS_FORMAT)));
        model.put("montant", operation.getMontant() + " EUR");
        model.put("moyenPaiement", moyenPaiement.getModepaiement());
        model.put("typeTransaction", operation.getOperationType());
        model.put("numCarte", moyenPaiement.getCbmasquee());
        model.put("numeroAutorisation", operation.getNumeroAutorisation());
        model.put("numeroTerminal", paiementPropertiesResolver.getTpe());
        return model;
    }


}
