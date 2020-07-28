package mc.gouv.xaf.back.service.es.handlers;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.es.model.EsErrorEventDTO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service permettant de handle les erreurs relatives à ES dans un contexte Transactionnel
 *
 * @author mpavone.ext
 */
@Primary
@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackOn = Exception.class)
public class EsTransactionErrorsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsTransactionErrorsHandler.class);

    private static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE";

    @Autowired
    PropertiesService propertiesService;

    @Autowired
    MailService mailService;

    @Autowired
    IndexedDemandeService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AfBackUtils afBackUtils;

    private final static String MAIL_TEMPLATE_ES_ROLLBACK_OBJET = "MAIL_TEMPLATE_ES_ROLLBACK_OBJET";
    private final static String MAIL_TEMPLATE_ES_ROLLBACK_CORPS = "MAIL_TEMPLATE_ES_ROLLBACK_CORPS";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleEsRollbackEvent(EsErrorEventDTO errorEventDTO) throws Exception {
        LOGGER.error("Erreur ES - Rollback de la BDD ");
        LOGGER.error("Récupération des adresses mails de contact");
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE);

        if (propertiesDTO.getValue() != null) {
            String[] adresses = propertiesDTO.getValue().trim().split(",");
            // Composition du mail
            Map<String, Object> model = new HashMap<>();
            model.put("errorEvent", errorEventDTO);
            EmailInfoDTO emailInfoDTO = createMailRollbackES(errorEventDTO);
            for (String adresseMail : adresses) {
                emailInfoDTO.addTo(adresseMail, "Support Technique");
            }
            mailService.sendMail(emailInfoDTO, model);
        }
    }

    private EmailInfoDTO createMailRollbackES(EsErrorEventDTO errorEventDTO) {
        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setBodyTemplateCode(MAIL_TEMPLATE_ES_ROLLBACK_CORPS);
        emailInfo.setSubjectTemplateCode(MAIL_TEMPLATE_ES_ROLLBACK_OBJET);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, errorEventDTO.getIdentifiantDemande());
        emailInfo.setLangue("fr");
        return emailInfo;
    }

    public static EsErrorEventDTO createErrorEvent(String contexte, DemandeDTO demandeDTO, Exception e) {
        EsErrorEventDTO errorEventDTO = new EsErrorEventDTO();
        errorEventDTO.setContexte(contexte);
        errorEventDTO.setDateTransaction(LocalDateTime.now());
        errorEventDTO.setDemandeId(demandeDTO.getPkDemandes());
        errorEventDTO.setDemarcheId(demandeDTO.getDemarcheId());
        errorEventDTO.setIdentifiantDemande(demandeDTO.getIdentifiant());
        errorEventDTO.setException(convertExceptionToHtmlString(e));
        return errorEventDTO;
    }

    public static EsErrorEventDTO createErrorEvent(String contexte, String demarcheId, Integer demandeId, Exception e) {
        EsErrorEventDTO errorEventDTO = new EsErrorEventDTO();
        errorEventDTO.setContexte(contexte);
        errorEventDTO.setDateTransaction(LocalDateTime.now());
        errorEventDTO.setDemandeId(demandeId);
        errorEventDTO.setDemarcheId(demarcheId);
        errorEventDTO.setException(convertExceptionToHtmlString(e));
        return errorEventDTO;
    }

    private static String convertExceptionToHtmlString(Exception e) {
        // Truncate à 3000 caractères car les mails doivent avoir < 3900 chars
        String strException = ExceptionUtils.getStackTrace(e);
        strException = strException.replace("\n", "<br/>").replace("\t", "&nbsp;&nbsp;");
        if (strException.length() > 3000) {
            strException = "...<br/>" + strException.substring(strException.length() - 3000);
        }
        return strException;
    }
}
