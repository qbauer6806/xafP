package mc.gouv.xaf.back.service.itg.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;

/**
 * Composant permettant l'envoi d'emails "templatés"
 *
 * @author qdeme
 */
public interface MailService {

    /**
     * Permet d'envoyer un email avec substitution de variables (templating)
     */
    void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException;

    void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, MailAudienceEnum audienceMail)
            throws JsonProcessingException;

    /**
     * Permet d'envoyer un email avec substitution de variables (templating) + pièces jointes
     */
    void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments)
            throws JsonProcessingException;

    void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments,
            MailAudienceEnum audienceMail) throws JsonProcessingException;

    /**
     * Permet d'obtenir un aperçu de l'email qui serait envoyé
     *
     * @return [0] contient le titre, [1] contient le contenu en HTML
     */
    String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue,
            Map<String, Object> model) throws IOException;

    /**
     * Permet de formater le commentaire afin d'y ajouter les sauts de lignes
     */
    String formatCommentaire(String commentaire);

    /**
     * Envois un mail typé support
     *
     * @param subjectTemplateCode
     *         objet tu mail
     * @param bodyTemplateCode
     *         corps du mail
     * @param mailingLists
     *         liste de emails à envoyer
     * @param pkDemande
     *         clé primaire de la demande
     * @param identifiantDemande
     *         demande identifiant
     * @param incident
     *         numéro incident (peut être nul)
     * @param modelAdd
     *         model éventuel à rajouter (peut être nul)
     * @param attachments
     *         attachments (peut être null)
     */
    void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, Set<String> mailingLists,
            Integer pkDemande, String identifiantDemande, int incident, Map<String, Object> modelAdd,
            Map<String, InputStream> attachments);

    /**
     * Permet de récupérer une liste d'emails à partir d'une ou plusieurs propriétés (liste de mails)
     *
     * @param mailingListProps
     *         Clés des propriétés
     * @return Liste concaténée de mails à partir de propriétés
     */
    Set<String> getMailingLists(String... mailingListProps);

}
