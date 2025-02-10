package mc.gouv.xaf.back.service.itg.sms.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.ToolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.itg.sms.SmsDTO;
import mc.gouv.xaf.back.service.itg.sms.SmsInfoDTO;
import mc.gouv.xaf.back.service.itg.sms.SmsParamDTO;
import mc.gouv.xaf.back.service.itg.sms.SmsService;
import mc.gouv.xaf.back.service.itg.sms.SmsTransformer;
import mc.gouv.xaf.back.service.templates.SmsTemplatesCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * Composant permettant l'envoi de SMS "templatés"
 *
 * @author qdeme
 */
@Component
public class SmsServiceImpl implements SmsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceImpl.class);
	
    @Autowired
    private SmsTemplatesCache smsTemplatesCache;

    @Autowired
    private AfBackUtils afBackUtils;

    private ToolManager manager = new ToolManager();
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

	@Override
	public SmsDTO getSms(String identifiant) {
		LOGGER.debug("SmsServiceImpl.getSms({})", identifiant);
		return afBackUtils.getSmsClient().getSms(identifiant);
	}
	
    private SmsDTO createSmsContent(SmsInfoDTO smsInfo, Map<String, Object> model) {
        String[] senderAndBody;
        try {
        	senderAndBody = getBodyAndSender(smsInfo.getBodyTemplateCode(),
        			smsInfo.getLangue(), model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération du corps du SMS et du sender", e);
            return null;
        }

        LOGGER.info("Transformation des informations de SMS vers les structures pour l'API SMS...");
        List<SmsParamDTO> params = SmsTransformer.toSmsApiParams(smsInfo.getParams());

        SmsDTO sms = new SmsDTO();
        sms.setTo(smsInfo.getTo());
        sms.setText(senderAndBody[0]);
        sms.setSender(senderAndBody[1]);
        sms.setParams(params);
        
        if (sms.getSender() != null && "".equals(sms.getSender())) {
        	sms.setSender(null);
        }

        return sms;
    }
    
    private String[] getBodyAndSender(String bodyTemplateCode, String langue,
            Map<String, Object> model) throws IOException {

        LOGGER.info("Récupération du template demandé pour le corps du SMS...");
        SmsTemplateDTO smsTemplateBody = smsTemplatesCache.getTemplate(bodyTemplateCode, langue);

        LOGGER.info("Appel à Velocity pour le templating du corps du SMS...");
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_INSTANCE, LOGGER);
        Velocity.init();
        Context context = getContext();
        if (model != null) {
            for (Map.Entry<String, Object> entry : model.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
        }
        StringWriter output = new StringWriter();
        if (!Velocity.evaluate(context, output, smsTemplateBody.getCode(), smsTemplateBody.getContenu())) {
            throw new DemarchesServiceException("Velocity.evaluate() pour le contenu du body n'a pas fonctionné.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String smsBodyToSend = output.toString();

        return new String[] { smsBodyToSend, smsTemplateBody.getSender() };
    }
    
    @Override
    public SmsDTO sendSms(SmsInfoDTO smsInfo, Map<String, Object> model) {
        LOGGER.debug("SmsServiceImpl.sendSms({}, {})", smsInfo, model);

        SmsDTO sms = createSmsContent(smsInfo, model);
        if (sms == null) {
            return null;
        }
        
        SmsDTO smsEnvoye = null;
        
        if (gouvPropertiesResolver.getSmsEnabled()) {        
	        LOGGER.info("Appel à SMS pour envoi du SMS...");
	        SmsClient smsClient = afBackUtils.getSmsClient();
	        smsEnvoye = smsClient.sendSms(sms);
        }
        else {
        	LOGGER.info("Envoi d'SMS désactivé. Log des informations de SMS pour débug : {}", sms);
        	smsEnvoye = new SmsDTO();
        	smsEnvoye.setIdentifiant("TEST");
        	smsEnvoye.setParams(new ArrayList<>());
        	smsEnvoye.setSender("");
        	smsEnvoye.setStatusLibel(AfBackUtils.SMS_ENVOYE_STATUT);
        	smsEnvoye.setText(sms.getText());
        	smsEnvoye.setTo(sms.getTo());
        }
        
        return smsEnvoye;
    }
    
    private Context getContext() {
        Context context = manager.createContext();
        context.put("StringUtils", StringUtils.class);
        return context;
    }

}