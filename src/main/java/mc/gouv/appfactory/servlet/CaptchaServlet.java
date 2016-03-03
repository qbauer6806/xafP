package mc.gouv.appfactory.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

/**
 * 
 * Servlet permettant d'effectuer le Captcha
 * 
 * @author qdeme
 *
 */
public class CaptchaServlet extends HttpServlet {

    private static final long serialVersionUID = 3902949116551411125L;
    
    private static Logger LOGGER = LoggerFactory.getLogger(CaptchaServlet.class);
    
    private static final String CAPTCHA_TOKEN_REGEXP = "^recaptcha_([0-9.]+)_(.*)_(.*)$";
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("/captcha doGet()");
        
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token) || !isValidToken(token)) {
            LOGGER.error("Token au mauvais format");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        String[] params = token.split("_");
        
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        
        // Initialisation de la clef privée pour la vérification du CAPTCHA via les properties
        reCaptcha.setPrivateKey(Static.getValue("XXXprivateKey.keyName", "XXXprivateKey.defaultValue"));
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(params[1], params[2], params[3]);
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{ \"valid\" : \"" + String.valueOf(reCaptchaResponse.isValid()) + "\" }");
        
        LOGGER.info("Fin /captcha doGet()");
    }
    
    private boolean isValidToken(String token) {
        return Pattern.compile(CAPTCHA_TOKEN_REGEXP).matcher(token).matches();
    }
    
}
