package mc.gouv.af.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.mail.apiclient.client.MailClient;
import mc.gouv.mail.shared.dto.AddressBlockDTO;
import mc.gouv.mail.shared.dto.MailDTO;
import mc.gouv.mail.shared.dto.MailSentDTO;

public class ContactServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -6944883275123392719L;

    private static Logger LOGGER = LoggerFactory.getLogger(ContactServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /contact doPost()");

        String captcha = request.getParameter("captcha");
        String emailAddress = request.getParameter("email");
        String titre = request.getParameter("titre");
        String message = request.getParameter("message");

        try {

            // Si l'utilisateur n'est pas logué, alors il faut vérifier le Captcha
            UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
            if (usagerInfosDTO == null) {
                // 1ère étape : vérification du Captcha
                LOGGER.info("Utilisateur non logué, vérification du Captcha...");
                if (!AppFactoryServletUtils.checkCaptcha(captcha)) {
                    LOGGER.error("Captcha invalide");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            } else {
                LOGGER.info("Utilisateur logué, pas de vérification du Captcha");
            }

            // 2ème étape : envoi du mail
            LOGGER.info("Envoi de l'email...");
            MailClient mc = new MailClient(AfServletGouvPropertiesResolver.getMailUrl(), AfServletGouvPropertiesResolver.getMailJwt());
            MailDTO email = new MailDTO();
            email.setFrom(new AddressBlockDTO(AfServletGouvPropertiesResolver.getGouvContactEmailExpediteurAdresse(), AfServletGouvPropertiesResolver.getGouvContactEmailExpediteurNom()));
            email.setTo(new AddressBlockDTO[] { new AddressBlockDTO(emailAddress, null) });
            email.setBcc(new AddressBlockDTO[] {
                    new AddressBlockDTO(AfServletGouvPropertiesResolver.getGouvContactEmailServiceAdresse(), AfServletGouvPropertiesResolver.getGouvContactEmailServiceNom()) });
            email.setReplyto(new AddressBlockDTO(emailAddress, null));
            email.setText(message);
            email.setSubject(titre);
            MailSentDTO es = mc.sendEmail(email);

            LOGGER.info("Email envoyé : " + es);

        } catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Erreur interne: ", e);
        }

        LOGGER.info("====================== Fin /contact doPost()");
    }

}
