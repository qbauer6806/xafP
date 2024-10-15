package mc.gouv.xaf.apiclient.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient.authentication.impl.BasicAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.shared.dto.mail.MailAddrOnlyDTO;
import mc.gouv.xaf.shared.dto.mail.MailDTO;
import mc.gouv.xaf.shared.dto.mail.MailSentDTO;
import mc.gouv.xaf.shared.exception.XafException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe cliente permettant d'appeler le WS MAIL
 * 
 * @author qdeme
 *
 */
public class MailClient extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    private static final String AUTHORIZATION = "Authorization";

    /**
     * Crée une instance du client avec sécurisation via Basic Auth
     * @param serviceUrl URL du WS à appeler
     * @param user User à utiliser pour l'authentification
     * @param password Mot de passe à utiliser pour l'authentification
     */
    public MailClient(String serviceUrl, String user, String password) {
        super(serviceUrl, new BasicAuthorizationHeaderProvider(user, password), true);
    }

    /**
     * Création d'une instance de mail client avec sécurisation via JWT
     * @param serviceUrl
     * @param jwtToken
     */
    public MailClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken), true);
    }

    /**
     * Permet d'envoyer un email avec (ou sans) pièces jointes
     * @param mailDTO L'email à envoyer
     * @param attachments Les pièces jointes à envoyer
     * @return Le résultat du WS (ID de l'email et son statut)
     */
    public MailSentDTO sendEmail(MailDTO mailDTO, Map<String, InputStream> attachments) {

        LOGGER.debug("sendEmail({},{})", mailDTO, attachments);

        boolean pj = attachments != null;
        
        if (pj) {
            
            try(MultiPart multiPartEntity = new MultiPart()) {
                multiPartEntity.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

                // Objet "email"
                ObjectMapper mapper = new ObjectMapper();
                String emailJson = mapper.writeValueAsString(mailDTO);
                LOGGER.info("JSON de l'email : {}", emailJson);
                FormDataBodyPart emailPart = new FormDataBodyPart("email", emailJson);
                multiPartEntity.bodyPart(emailPart);

                // Pièces jointes
                for (Map.Entry<String, InputStream> entry : attachments.entrySet()) {
                    String filename = entry.getKey();
                    InputStream fileContent = entry.getValue();
                    LOGGER.info("Ajout d'une pièce jointe : {}", filename);
                    StreamDataBodyPart fileDataBodyPart = new StreamDataBodyPart("file", fileContent, filename);
                    multiPartEntity.bodyPart(fileDataBodyPart);
                }

                LOGGER.debug("Appel ({}, pj={})...", getServiceUrl(), pj);

                return getTarget().request()
                        .header(AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                        .post(Entity.entity(multiPartEntity, multiPartEntity.getMediaType()), MailSentDTO.class);
            } catch (IOException e) {
                throw new XafException(e);
            }
        }
        else {

            LOGGER.debug("Appel ({}, pj={})...", getServiceUrl(), pj);
            
            return getTarget().request()
                    .header(AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                    .post(Entity.entity(mailDTO, MediaType.APPLICATION_JSON), MailSentDTO.class);
        }
    }

    /**
     * Permet d'envoyer un email sans pièces jointes
     * @param mailDTO L'email à envoyer
     * @return Le résultat du WS (ID de l'email et son statut)
     */
    public MailSentDTO sendEmail(MailDTO mailDTO) {

        LOGGER.debug("sendEmail({})", mailDTO);

        return sendEmail(mailDTO, null);
    }

    /**
     * Retrouve un email à partir de son ID
     * @param id ID de l'email à retrouver
     * @return L'email demandé
     */
    public MailDTO getEmail(Integer id) {

        LOGGER.info("getEmail({})", id);

        return getTarget().path(Integer.toString(id)).request()
                .header(AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get(MailDTO.class);
    }

    /**
     * Retrouve des emails à partir d'une métadonnée
     * @param metaKey
     * @param metaValue
     * @return L'email demandé
     */
    public List<MailDTO> getEmails(String metaKey, String metaValue) {

        LOGGER.info("getEmails({},{})", metaKey, metaValue);

        return getTarget().path("/").queryParam("metaKey", metaKey).queryParam("metaValue", metaValue)
                .request()
                .header(AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .get(new GenericType<List<MailDTO>>() {
                });
    }

    /**
     *
     * @param emailId
     * @return
     */
    public MailSentDTO resendEmail(Integer emailId) {

        LOGGER.info("resendEmail({})", emailId);

        return resendEmail(emailId, null);
    }

    public MailSentDTO resendEmail(Integer emailId, MailAddrOnlyDTO mailAddrOnlyDTO) {

        LOGGER.info("resendEmail({},{})",emailId, mailAddrOnlyDTO);
        MailDTO mailDTO = getEmail(emailId);

        // Si le client souhaite renvoyer en changeant des adresses...
        if (mailAddrOnlyDTO != null) {
            mailDTO.setTo(mailAddrOnlyDTO.getTo());
            mailDTO.setBcc(mailAddrOnlyDTO.getBcc());
            mailDTO.setCc(mailAddrOnlyDTO.getCc());
            mailDTO.setReplyto(mailAddrOnlyDTO.getReplyto());
            mailDTO.setFrom(mailAddrOnlyDTO.getFrom());
        }

        // renvoyer
        return sendEmail(mailDTO);
    }

}
