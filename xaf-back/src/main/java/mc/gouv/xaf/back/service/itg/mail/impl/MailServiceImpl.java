package mc.gouv.xaf.back.service.itg.mail.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.mail.apiclient.client.MailClient;
import mc.gouv.mail.shared.dto.AddressBlockDTO;
import mc.gouv.mail.shared.dto.MailDTO;
import mc.gouv.mail.shared.dto.ParamDTO;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.EmailTransformer;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.shared.enums.MailTemplateAudienceEnum;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * Composant permettant l'envoi d'emails "templatés"
 *
 * @author qdeme
 *
 */
@Component
public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String XAF_NOTIFICATION_MAIL_AGENT = "XAF_NOTIFICATION_MAIL_AGENT";

    private ToolManager manager = new ToolManager();

    @Autowired
    private TemplatesCache templatesCache;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException {
        sendMail(emailInfo, model, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments) throws JsonProcessingException {
        LOGGER.info("MailServiceImpl.sendMail({}, {}, {})", emailInfo, model, attachments);
        if (this.estTemplateMailAgentSansEnvoiMail(emailInfo)) {
            LOGGER.info("PAS d'envoi email aux agents du service {}", gouvPropertiesResolver.getDemarcheId());
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

    private boolean estTemplateMailAgentSansEnvoiMail(EmailInfoDTO emailInfo) {
        TemplateDTO templateBody = templatesCache.getTemplate(emailInfo.getBodyTemplateCode(), emailInfo.getLangue());
        if (templateBody == null || !MailTemplateAudienceEnum.AGENT.equals(templateBody.getAudience())) {
            //il ne s'agit pas d'un template mail agent
            return false;
        }
        PropertiesDTO mailProperty =
                propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_NOTIFICATION_MAIL_AGENT);
        return mailProperty != null && !BooleanUtils.toBoolean(mailProperty.getValue());
    }

    private MailDTO createMailContent(EmailInfoDTO emailInfo, Map<String, Object> model) {
        String[] subjectAndBody;
        try {
            subjectAndBody = getSubjectAndBody(emailInfo.getSubjectTemplateCode(), emailInfo.getBodyTemplateCode(), emailInfo.getLangue(), model);
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
        email.setTo(to.toArray(new AddressBlockDTO[to.size()]));
        email.setCc(cc.toArray(new AddressBlockDTO[cc.size()]));
        email.setBcc(bcc.toArray(new AddressBlockDTO[bcc.size()]));
        email.setFrom(from);
        email.setReplyto(replyTo);
        email.setParams(params.toArray(new ParamDTO[params.size()]));
        email.setSubject(subjectAndBody[0]);
        email.setHtml(subjectAndBody[1]);
        // Pas de email.setText() ==> on considère que les templates body des démarches sont toujours en HTML !

        return email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue, Map<String, Object> model) throws Exception {
        LOGGER.info("MailServiceImpl.getMailPreview({},{})", bodyTemplateCode, subjectTemplateCode);
        return getSubjectAndBody(subjectTemplateCode, bodyTemplateCode, langue, model);
    }
    
    private String[] getSubjectAndBody(String subjectTemplateCode, String bodyTemplateCode, String langue, Map<String, Object> model) {
        
        LOGGER.info("Récupération du template demandé pour le corps de l'email...");
        TemplateDTO templateBody = templatesCache.getTemplate(bodyTemplateCode, langue);

        LOGGER.info("Récupération du template demandé pour le sujet de l'email...");
        TemplateDTO templateSubject = templatesCache.getTemplate(subjectTemplateCode, langue);

        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet de l'email...");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, LOGGER);
        Velocity.init();
        Context context = getContext();
        if (model != null) {
            for (Map.Entry<String,Object> entry : model.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        StringWriter output = new StringWriter();
        if (!Velocity.evaluate(context, output, templateBody.getCode(), templateBody.getContenu())) {
            throw new DemarchesServiceException("Velocity.evaluate() n'a pas fonctionné.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String mailBodyToSend = output.toString();
        output = new StringWriter();
        if (!Velocity.evaluate(context, output, templateSubject.getCode(), templateSubject.getContenu())) {
            throw new DemarchesServiceException("Velocity.evaluate() n'a pas fonctionné.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String mailSubjectToSend = output.toString();
        return new String[] { mailSubjectToSend, mailBodyToSend };
    }
    
    private Context getContext() {
        Context context = manager.createContext();
        context.put("StringUtils", StringUtils.class);
        context.put("date", new DateTool());
        return context;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String formatCommentaire(String commentaire) {
		if (!StringUtils.isBlank(commentaire)) {
			String lineSep = System.getProperty("line.separator");
			commentaire = commentaire.replace(lineSep, "<br/>");
		}
		return commentaire;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, Set<String> mails,Integer pkDemande,
                                String identifiantDemande, int incident, Map<String, Object> modelAdd, Map<String, InputStream> attachments) {
        Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
        String dateTimeString = simpleDateTimeFormat.format(date);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setLangue("fr");
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, identifiantDemande);

        for (String adresseMail : mails) {
            emailInfo.addTo(adresseMail, "Support Technique");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("identifiant", identifiantDemande);
        model.put("Pkdemandes", pkDemande);
        if(modelAdd != null) {
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
        for(String mailProp : mailingListProps) {
            PropertiesDTO mailProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), mailProp);
            if (mailProperty != null && StringUtils.isNotBlank(mailProperty.getValue())) {
                list.addAll(Arrays.asList(mailProperty.getValue().trim().split(",")));
            }
        }
        return list;
    }

}
