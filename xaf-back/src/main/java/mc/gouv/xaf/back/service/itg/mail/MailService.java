package mc.gouv.xaf.back.service.itg.mail;

import java.io.IOException;
import java.util.Map;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * Composant permettant l'envoi d'emails "templatés"
 * 
 * @author qdeme
 *
 */
public interface MailService {

	/**
	 * Permet d'envoyer un email avec substitution de variables (templating)
	 * 
	 * @param emailInfo
	 * @param model
	 * @throws JsonProcessingException 
	 * @throws Exception
	 */
	public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException;

	/**
	 * Permet d'obtenir un aperçu de l'email qui serait envoyé
	 * 
	 * @param bodyTemplateCode
	 * @param subjectTemplateCode
	 * @param langue
	 * @param model
	 * @return [0] contient le titre, [1] contient le contenu en HTML
	 * @throws IOException 
	 * @throws ResourceNotFoundException 
	 * @throws MethodInvocationException 
	 * @throws ParseErrorException 
	 * @throws Exception
	 */
	public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue,
			Map<String, Object> model) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, Exception;

	/**
	 * Permet de formater le commentaire afin d'y ajouter les sauts de lignes
	 * 
	 * @param commentaire
	 * @return
	 */
	public String formatCommentaire(String commentaire);

}
