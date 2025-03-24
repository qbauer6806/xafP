package mc.gouv.xaf.back.service.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Service permettant de handle les erreurs dans un contexte Transactionnel
 *
 * @author mpavone.ext
 */
@Service
@Transactional(rollbackOn = Exception.class)
public class TransactionErrorsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionErrorsHandler.class);
    private static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";
    private static final String MAIL_ROLLBACK_OBJET = "MAIL_ROLLBACK_OBJET";
    private static final String MAIL_ROLLBACK_CORPS = "MAIL_ROLLBACK_CORPS";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private MailService mailService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AfBackUtils afBackUtils;

    public ErrorEventDTO createErrorEvent(String contexte, List<DemandeBO> demandeBOS, Exception e) {
        String[] tab = preparerMail(demandeBOS);
        ErrorEventDTO errorEventDTO = new ErrorEventDTO();
        errorEventDTO.setContexte(contexte);
        errorEventDTO.setDateTransaction(LocalDateTime.now().format(formatter));
        errorEventDTO.setDemandeIds(tab[0]);
        errorEventDTO.setPhraseDemandes(tab[1]);
        errorEventDTO.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        errorEventDTO.setException(convertExceptionToHtmlString(e));
        return errorEventDTO;
    }

    public ErrorEventDTO createErrorEvent(String contexte, Integer demandeId, Exception e) {
        ErrorEventDTO errorEventDTO = new ErrorEventDTO();
        errorEventDTO.setContexte(contexte);
        errorEventDTO.setDateTransaction(LocalDateTime.now().format(formatter));
        errorEventDTO.setPhraseDemandes("La demande impactée avait pour id : <b>" + demandeId + "</b>.<br/>");
        errorEventDTO.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        errorEventDTO.setException(convertExceptionToHtmlString(e));
        return errorEventDTO;
    }

    public ErrorEventDTO createErrorEvent(String contexte, Exception e) {
        ErrorEventDTO errorEventDTO = new ErrorEventDTO();
        errorEventDTO.setContexte(contexte);
        errorEventDTO.setDateTransaction(LocalDateTime.now().format(formatter));
        errorEventDTO.setPhraseDemandes("");
        errorEventDTO.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        errorEventDTO.setException(convertExceptionToHtmlString(e));
        return errorEventDTO;
    }

    private String[] preparerMail(List<DemandeBO> demandeBOS) {
        StringBuilder phraseBuilder = new StringBuilder("Les demandes suivantes sont impactées :<br><ul>");
        StringJoiner listeIdDemandes = new StringJoiner(",");
        for (DemandeBO d : demandeBOS) {
            listeIdDemandes.add("" + d.getPkDemandes());
            phraseBuilder.append("<li><b>")
                    .append(d.getIdentifiant())
                    .append("</b> qui avait pour id : <b>")
                    .append(d.getPkDemandes())
                    .append("</b>.</li>");
        }
        phraseBuilder.append("</ul>");
        return new String[]{listeIdDemandes.toString(), phraseBuilder.toString()};
    }

    private String convertExceptionToHtmlString(Exception e) {
        // Truncate à 3000 caractères car les mails doivent avoir < 3900 chars
        String strException = ExceptionUtils.getStackTrace(e);
        strException = strException.replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;");
        if (strException.length() > 3000) {
            strException = strException.substring(0, 3000) + "...<br/>";
        }
        return strException;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRollbackEvent(ErrorEventDTO errorEventDTO) throws JsonProcessingException {
        LOGGER.error("Erreur - Rollback de la BDD ");
        LOGGER.error("Récupération des adresses mails de contact");
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);

        if (propertiesDTO.getValue() != null) {
            String[] adresses = propertiesDTO.getValue().trim().split(",");
            // Composition du mail
            Map<String, Object> model = new HashMap<>();
            model.put("errorEvent", errorEventDTO);
            EmailInfoDTO emailInfoDTO = createMailRollback(errorEventDTO);
            for (String adresseMail : adresses) {
                emailInfoDTO.addTo(adresseMail, "Support Technique");
            }
            mailService.sendMail(emailInfoDTO, model);
        }
    }

    private EmailInfoDTO createMailRollback(ErrorEventDTO errorEventDTO) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(MAIL_ROLLBACK_CORPS);
        emailInfo.setSubjectTemplateCode(MAIL_ROLLBACK_OBJET);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, errorEventDTO.getDemandeIds());
        emailInfo.setLangue("fr");
        return emailInfo;
    }
}
