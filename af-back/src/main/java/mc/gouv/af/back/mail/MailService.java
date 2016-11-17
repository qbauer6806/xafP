package mc.gouv.af.back.mail;

import java.util.Map;

/**
 * 
 * Composant permettant l'envoi d'emails "templatés"
 * 
 * @author qdeme
 *
 */
public interface MailService {

    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws Exception;
    
}
