package mc.gouv.xaf.back.service.itg.sms;

import java.util.Map;

/**
 * Composant permettant l'envoi de SMS "templatés"
 *
 * @author qdeme
 */
public interface SmsService {

	public SmsDTO sendSms(SmsInfoDTO sms, Map<String, Object> model);
	
	public SmsDTO getSms(String identifiant);
	
}
