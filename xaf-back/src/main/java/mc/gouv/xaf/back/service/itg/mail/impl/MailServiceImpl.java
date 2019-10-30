package mc.gouv.xaf.back.service.itg.mail.impl;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.generic.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.EmailTransformer;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.mail.shared.dto.AddressBlockDTO;
import mc.gouv.mail.shared.dto.MailDTO;
import mc.gouv.mail.shared.dto.ParamDTO;

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

    private ToolManager manager = new ToolManager();

    @Autowired
    private TemplatesCache templatesCache;
    
    @Autowired
    private AfBackUtils afBackUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws Exception {

        LOGGER.info("MailServiceImpl.sendMail(" + emailInfo + "," + model + ")");

        String[] subjectAndBody = getSubjectAndBody(emailInfo.getSubjectTemplateCode(), emailInfo.getBodyTemplateCode(), emailInfo.getLangue(), model);

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

        LOGGER.info("Appel à MAIL pour envoi de l'email...");
        afBackUtils.getMailClient().sendEmail(email);

    }

    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue, Map<String, Object> model) throws Exception {
        LOGGER.info("MailServiceImpl.getMailPreview(" + bodyTemplateCode + "," + subjectTemplateCode + ")");

        return getSubjectAndBody(subjectTemplateCode, bodyTemplateCode, langue, model);
    }
    
    private String[] getSubjectAndBody(String subjectTemplateCode, String bodyTemplateCode, String langue, Map<String, Object> model) throws Exception {
        
        LOGGER.info("Récupération du template demandé pour le corps de l'email...");
        TemplateDTO templateBody = templatesCache.getTemplate(bodyTemplateCode, langue);

        LOGGER.info("Récupération du template demandé pour le sujet de l'email...");
        TemplateDTO templateSubject = templatesCache.getTemplate(subjectTemplateCode, langue);

        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet de l'email...");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Velocity.init();
        Context context = getContext();
        if (model != null) {
            for (String key : model.keySet()) {
                context.put(key, model.get(key));
            }
        }

        StringWriter output = new StringWriter();

        if (!Velocity.evaluate(context, output, templateBody.getCode(), templateBody.getContenu())) {
            throw new Exception("Velocity.evaluate() n'a pas fonctionné.");
        }
        String mailBodyToSend = output.toString();
        output = new StringWriter();
        if (!Velocity.evaluate(context, output, templateSubject.getCode(), templateSubject.getContenu())) {
            throw new Exception("Velocity.evaluate() n'a pas fonctionné.");
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

}
