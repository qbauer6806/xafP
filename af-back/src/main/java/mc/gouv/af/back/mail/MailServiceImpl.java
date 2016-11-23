package mc.gouv.af.back.mail;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.back.util.TemplatesCache;
import mc.gouv.dem.apishared.model.TemplateDTO;
import mc.gouv.mail.apiclient.client.MailClient;
import mc.gouv.mail.apishared.model.AddressBlock;
import mc.gouv.mail.apishared.model.Email;
import mc.gouv.mail.apishared.model.Param;

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
    
    private MailClient mailClient = null;
    
    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private TemplatesCache templatesCache;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws Exception {

        LOGGER.info("MailServiceImpl.sendMail(" + emailInfo + "," + model + ")");
        
        LOGGER.info("Récupération du template demandé pour le corps de l'email...");
        TemplateDTO templateBody = templatesCache.getTemplate(emailInfo.getBodyTemplateCode(), emailInfo.getLangue());
        
        LOGGER.info("Récupération du template demandé pour le sujet de l'email...");
        TemplateDTO templateSubject = templatesCache.getTemplate(emailInfo.getSubjectTemplateCode(), emailInfo.getLangue());
        
        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet de l'email...");
        Context context = manager.createContext();
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
        
        LOGGER.info("Transformation des informations d'email vers les structures pour MAIL...");
        List<AddressBlock> to = EmailTransform.toMailApiAddresses(emailInfo.getTo());
        List<AddressBlock> cc = EmailTransform.toMailApiAddresses(emailInfo.getCc());
        List<AddressBlock> bcc = EmailTransform.toMailApiAddresses(emailInfo.getBcc());
        AddressBlock from = EmailTransform.toMailApiAddress(emailInfo.getFrom());
        AddressBlock replyTo = EmailTransform.toMailApiAddress(emailInfo.getReplyto());
        List<Param> params = EmailTransform.toMailApiParams(emailInfo.getParams());
        
        Email email = new Email();
        email.setTo(to.toArray(new AddressBlock[to.size()]));
        email.setCc(cc.toArray(new AddressBlock[cc.size()]));
        email.setBcc(bcc.toArray(new AddressBlock[bcc.size()]));
        email.setFrom(from);
        email.setReplyto(replyTo);
        email.setParams(params.toArray(new Param[params.size()]));
        email.setSubject(mailSubjectToSend);
        email.setHtml(mailBodyToSend);
        // Pas de email.setText() ==> on considère que les templates body des démarches sont toujours en HTML !
        
        LOGGER.info("Appel à MAIL pour envoi de l'email...");
        getMailClient().sendEmail(email);
        
    }
    
    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue, Map<String, Object> model) throws Exception {
        LOGGER.info("MailServiceImpl.getMailPreview(" + bodyTemplateCode + "," + subjectTemplateCode + "," + langue + "," + model + ")");
        
        LOGGER.info("Récupération du template demandé pour le corps de l'email...");
        TemplateDTO templateBody = templatesCache.getTemplate(bodyTemplateCode, langue);
        
        LOGGER.info("Récupération du template demandé pour le sujet de l'email...");
        TemplateDTO templateSubject = templatesCache.getTemplate(subjectTemplateCode, langue);
        
        LOGGER.info("Appel à Velocity pour le templating du corps et du sujet de l'email...");
        Context context = manager.createContext();
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
        
        return new String[] { mailSubjectToSend , mailBodyToSend };
    }
    
    /**
     * Initialisation du MailClient si pas déjà fait
     */
    private MailClient getMailClient() {
        if (mailClient == null) {
            mailClient = new MailClient(afBackUtils.getMailUrl(), afBackUtils.getMailUser(), afBackUtils.getMailPwd());
        }
        return mailClient;
    }


}
