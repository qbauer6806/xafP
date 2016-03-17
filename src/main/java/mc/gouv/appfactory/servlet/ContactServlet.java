package mc.gouv.appfactory.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.appfactory.util.AppFactoryServletUtils;
import mc.gouv.mail.api.client.MailClient;
import mc.gouv.mail.api.model.AddressBlock;
import mc.gouv.mail.api.model.Email;
import mc.gouv.mail.api.model.EmailSent;

public class ContactServlet extends HttpServlet {

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
            HttpSession session = request.getSession(false);
            if (session == null) {
                // 1ère étape : vérification du Captcha
                LOGGER.info("Utilisateur non logué, vérification du Captcha...");
                if (!AppFactoryServletUtils.checkCaptcha(captcha)) {
                    LOGGER.error("Captcha invalide");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            else {
                LOGGER.info("Utilisateur logué, pas de vérification du Captcha");
            }
            
            // 2ème étape : envoi du mail
            LOGGER.info("Envoi de l'email...");
            MailClient mc = new MailClient(AppFactoryServletUtils.MAIL_URL, AppFactoryServletUtils.MAIL_USER, AppFactoryServletUtils.MAIL_PWD);
            Email email = new Email();
            email.setFrom(new AddressBlock(emailAddress, null));
            email.setTo(new AddressBlock[] { new AddressBlock(AppFactoryServletUtils.GOUV_CONTACT_EMAIL, null) });
            email.setText(message);
            email.setSubject(titre);
            EmailSent es = mc.sendEmail(email);
    
            LOGGER.info("Email envoyé : " + es);
        
        }
        catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Erreur interne: " + e.toString() + " / " + e.getMessage());
        }
        
        LOGGER.info("====================== Fin /contact doPost()");
    }

}
