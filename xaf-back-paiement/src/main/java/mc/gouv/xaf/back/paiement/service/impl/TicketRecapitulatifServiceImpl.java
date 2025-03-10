package mc.gouv.xaf.back.paiement.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.TicketRecapitulatifService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.impl.AfMailTemplateModelProvider;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private AfMailTemplateModelProvider afMailTemplateModelProvider;

    @Override
    public void sendMail(CommandeOperationDTO operation, CommandeDTO commandeDTO, Integer demandeId) {

        Map<String, Object> variables = gouvBPM.getProcessBusinessVariables(demandeId);
        Integer usagerId = (Integer) variables.get(GouvBPMProcessVariableTypeEnum.MC_USAGERID.name());

        GichuniUsagerDTO usager = usagersCache.get(usagerId, true);
        DemandeDTO demandeDto = demandesService.getDemande(demandeId);
        if (usager == null) {
            usager = new GichuniUsagerDTO();
            DemandeUsagerDTO usagerDto = demandeDto.getUsager();
            if (usagerDto != null) {
                usager.setNom(usagerDto.getNom());
                usager.setPrenom(usagerDto.getPrenom());
                usager.setEmail(usagerDto.getEmail());
            }
        }

        String bodyTemplateCode = "MAIL_TICKET_RECAP_USAGER_CORPS";
        String subjectTemplateCode = "MAIL_TICKET_RECAP_USAGER_OBJET";

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addTo(usager.getEmail(), usager.getPrenom() + " " + usager.getNom());
        emailInfo.setLangue(demandeDto.getLangue());

        try {
            Map<String, Object> model = getModel(operation, commandeDTO.getMoyenPaiement(), demandeDto);
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private Map<String, Object> getModel(CommandeOperationDTO operation, MoyenPaiementDTO moyenPaiement,
            DemandeDTO demande) {
        Map<String, Object> model = afMailTemplateModelProvider.getGenericModelDemande(demande);
        model.put("numTPE", paiementPropertiesResolver.getTpe());
        model.put("pkOperation", operation.getPkOperations());
        model.put("reference", moyenPaiement.getPkMoyenPaiements());
        model.put("dateTransaction",
                operation.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        model.put("montant", operation.getMontant() + " EUR");
        model.put("moyenPaiement", moyenPaiement.getModepaiement());
        model.put("typeTransaction", operation.getOperationType());
        model.put("numCarte", moyenPaiement.getCbmasquee());
        model.put("numeroAutorisation",
                null == operation.getNumeroAutorisation() ? "" : operation.getNumeroAutorisation());
        return model;
    }

}
