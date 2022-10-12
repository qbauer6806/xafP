package mc.gouv.xaf.back.service.itg.mail;

import java.io.IOException;
import java.util.List;
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


	/**
	 * Envois un mail typé support
	 * @param subjectTemplateCode objet tu mail
	 * @param bodyTemplateCode corps du mail
	 * @param mailingLists liste de emails à envoyer
	 * @param demandeId demande id
	 * @param incident numéro incident (peut être nul)
	 * @param modelAdd model éventuel à rajouter (peut être nul)
	 */
	void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, List<String> mailingLists, int demandeId, int incident, Map<String, Object> modelAdd);

	/**
	 * Permet de récupérer une liste d'emails à partir d'une ou plusieurs propriétés (liste de mails)
	 * @param mailingListProps Clés des propriétés
	 * @return Liste concaténée de mails à partir de propriétés
	 */
	List<String> getMailingLists(String... mailingListProps);

}
