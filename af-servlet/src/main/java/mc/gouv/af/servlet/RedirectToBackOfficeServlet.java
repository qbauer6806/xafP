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

public class RedirectToBackOfficeServlet extends AbstractAfServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -9158173804027496532L;
    private static Logger LOGGER = LoggerFactory.getLogger(RedirectToBackOfficeServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /redirect-to-backoffice doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        if (!usagerInfosDTO.isUsagerCourrier()) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé car non usager courrier");
            return;
        }

        response.sendRedirect(AfServletGouvPropertiesResolver.getBackOfficeUrl());

        LOGGER.info("====================== Fin /redirect-to-backoffice doGet()");
    }
}
