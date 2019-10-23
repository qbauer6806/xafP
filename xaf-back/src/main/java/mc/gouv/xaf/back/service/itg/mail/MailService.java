package mc.gouv.xaf.back.service.itg.mail;

import java.util.Map;

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
	 * @throws Exception
	 */
	public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws Exception;

	/**
	 * Permet d'obtenir un aperçu de l'email qui serait envoyé
	 * 
	 * @param bodyTemplateCode
	 * @param subjectTemplateCode
	 * @param langue
	 * @param model
	 * @return [0] contient le titre, [1] contient le contenu en HTML
	 * @throws Exception
	 */
	public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue,
			Map<String, Object> model) throws Exception;

	/**
	 * Permet de formater le commentaire afin d'y ajouter les sauts de lignes
	 * 
	 * @param commentaire
	 * @return
	 */
	public String formatCommentaire(String commentaire);

}
