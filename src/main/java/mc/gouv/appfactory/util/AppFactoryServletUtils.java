package mc.gouv.appfactory.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

public class AppFactoryServletUtils {
    
    public static HttpServletResponse logAndSendError(Logger logger, HttpServletResponse response, int httpStatus, String errMsg) throws IOException {
        logger.error(errMsg);
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.getOutputStream().write(("{ \"errors\" : [ { \"libelle\" : \"" + errMsg + "\" } ] }").getBytes());
        return response;
    }

}
