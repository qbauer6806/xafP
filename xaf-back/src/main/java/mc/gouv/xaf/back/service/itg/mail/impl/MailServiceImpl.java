package mc.gouv.xaf.back.service.itg.mail.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import mc.gouv.xaf.apiclient.mail.MailClient;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.EmailTransformer;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.dto.mail.AddressBlockDTO;
import mc.gouv.xaf.shared.dto.mail.MailDTO;
import mc.gouv.xaf.shared.dto.mail.ParamDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Composant permettant l'envoi d'emails "templatés"
 *
 * @author qdeme
 */
@Component
public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String XAF_NOTIFICATION_MAIL_AGENT = "XAF_NOTIFICATION_MAIL_AGENT";

    @Autowired
    private TemplatesCache templatesCache;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    @Qualifier("customTemplateEngine")
    private TemplateEngine templateEngine;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException {
        sendMail(emailInfo, model, null, null);
    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, MailAudienceEnum audienceMail)
            throws JsonProcessingException {
        sendMail(emailInfo, model, null, audienceMail);
    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments)
            throws JsonProcessingException {
        sendMail(emailInfo, model, attachments, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments,
            MailAudienceEnum audienceMail) throws JsonProcessingException {
        LOGGER.debug("MailServiceImpl.sendMail({}, {}, {}, {})", emailInfo, model, attachments, audienceMail);
        if (MailAudienceEnum.AGENT.equals(audienceMail) && !notificationMailAgentProperty()) {
            LOGGER.info("PAS d'envoi email aux agents du service");
            return;
        }
        MailDTO email = createMailContent(emailInfo, model);
        if (email == null) {
            return;
        }
        LOGGER.info("Appel à MAIL pour envoi de l'email...");
        MailClient mailClient = afBackUtils.getMailClient();
        mailClient.sendEmail(email, attachments);
    }

    private boolean notificationMailAgentProperty() {
        PropertiesDTO enableMailsAgentProperty = propertiesService.getProperty(XAF_NOTIFICATION_MAIL_AGENT);
        // Si la propriété n'existe pas alors on active les notifications mails agent par défaut
        if (enableMailsAgentProperty == null) {
            return true;
        }
        return BooleanUtils.toBoolean(enableMailsAgentProperty.getValue());
    }

    private MailDTO createMailContent(EmailInfoDTO emailInfo, Map<String, Object> model) {
        String[] subjectAndBody;
        try {
            subjectAndBody = getSubjectAndBody(emailInfo.getSubjectTemplateCode(), emailInfo.getBodyTemplateCode(),
                    emailInfo.getLangue(), model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération du corps et du sujet de l'e-mail", e);
            return null;
        }

        LOGGER.info("Transformation des informations d'email vers les structures pour MAIL...");
        List<AddressBlockDTO> to = EmailTransformer.toMailApiAddresses(emailInfo.getTo());
        List<AddressBlockDTO> cc = EmailTransformer.toMailApiAddresses(emailInfo.getCc());
        List<AddressBlockDTO> bcc = EmailTransformer.toMailApiAddresses(emailInfo.getBcc());
        AddressBlockDTO from = EmailTransformer.toMailApiAddress(emailInfo.getFrom());
        AddressBlockDTO replyTo = EmailTransformer.toMailApiAddress(emailInfo.getReplyto());
        List<ParamDTO> params = EmailTransformer.toMailApiParams(emailInfo.getParams());

        MailDTO email = new MailDTO();
        email.setTo(to.toArray(AddressBlockDTO[]::new));
        email.setCc(cc.toArray(AddressBlockDTO[]::new));
        email.setBcc(bcc.toArray(AddressBlockDTO[]::new));
        email.setFrom(from);
        email.setReplyto(replyTo);
        email.setParams(params.toArray(ParamDTO[]::new));
        email.setSubject(subjectAndBody[0]);
        email.setHtml(subjectAndBody[1]);
        // Pas de email.setText() ==> on considère que les templates body des démarches sont toujours en HTML !

        return email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue,
            Map<String, Object> model) throws IOException {
        LOGGER.info("MailServiceImpl.getMailPreview({},{})", bodyTemplateCode, subjectTemplateCode);
        return getSubjectAndBody(subjectTemplateCode, bodyTemplateCode, langue, model);
    }

    private String[] getSubjectAndBody(String subjectTemplateCode, String bodyTemplateCode, String langue,
            Map<String, Object> model) throws IOException {

        LOGGER.info("Récupération du template demandé pour le corps de l'email...");
        TemplateDTO templateBody = templatesCache.getTemplate(bodyTemplateCode, langue);

        LOGGER.info("Récupération du template demandé pour le sujet de l'email...");
        TemplateDTO templateSubject = templatesCache.getTemplate(subjectTemplateCode, langue);

        LOGGER.info("Appel à Thymeleaf pour le templating du corps et du sujet de l'email...");
        Context context = getContext(model);

        String mailBodyToSend = processTemplate(afBackUtils.convertToThymeleaf(templateBody.getContenu()), context);
        String mailSubjectToSend = processTemplate(afBackUtils.convertToThymeleaf(templateSubject.getContenu()),
                context);

        // Intégrer le corps de l'e-mail dans le template HTML de XAF si fonctionnalité activée
        if (afBackUtils.isEmailHtmlEnabled()) {
            LOGGER.info("Intégration du corps de l'email dans le template HTML de XAF...");
            context.setVariable("emailBodyToSend", mailBodyToSend);
            if ("en".equals(langue) && StringUtils.isNotBlank(afBackUtils.getDemarcheInfos().getNomEn())) {
                context.setVariable("titreTs", afBackUtils.getDemarcheNomEn());
            } else {
                context.setVariable("titreTs", afBackUtils.getDemarcheNom());
            }

            InputStream inputStream = new ClassPathResource("/email/email-template.html").getInputStream();
            String contenu = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            mailBodyToSend = processTemplate(contenu, context);
        }

        return new String[] { mailSubjectToSend, mailBodyToSend };
    }

    private Context getContext(Map<String, Object> model) {
        Context context = new Context();
        if (model != null) {
            context.setVariables(model);
        }
        return context;
    }

    private String processTemplate(String templateContent, Context context) {
        try {
            return templateEngine.process(templateContent, context);
        } catch (TemplateProcessingException e) {
            throw new DemarchesServiceException("Thymeleaf template processing failed.",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String formatCommentaire(String commentaire) {
        if (!StringUtils.isBlank(commentaire)) {
            String lineSep = System.lineSeparator();
            commentaire = commentaire.replace(lineSep, "<br/>");
        }
        return commentaire;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, Set<String> mails,
            Integer pkDemande, String identifiantDemande, int incident, Map<String, Object> modelAdd,
            Map<String, InputStream> attachments) {
        Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
        String dateTimeString = simpleDateTimeFormat.format(date);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setLangue("fr");
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(),
                afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(),
                afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, identifiantDemande);

        for (String adresseMail : mails) {
            emailInfo.addTo(adresseMail, "Support Technique");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("identifiant", identifiantDemande);
        model.put("Pkdemandes", pkDemande);
        if (modelAdd != null) {
            model.putAll(modelAdd);
        }
        try {
            sendMail(emailInfo, model, attachments);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getMailingLists(String... mailingListProps) {
        Set<String> list = new TreeSet<>();
        for (String mailProp : mailingListProps) {
            PropertiesDTO mailProperty = propertiesService.getProperty(mailProp);
            if (mailProperty != null && StringUtils.isNotBlank(mailProperty.getValue())) {
                list.addAll(Arrays.asList(mailProperty.getValue().trim().split(",")));
            }
        }
        return list;
    }

}
