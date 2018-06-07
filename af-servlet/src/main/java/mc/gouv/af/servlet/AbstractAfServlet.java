package mc.gouv.af.servlet;

import javax.servlet.http.HttpServlet;

import mc.gouv.af.apiclient.AfApiClient;
import mc.gouv.af.servlet.properties.AfServletGouvPropertiesResolver;

public class AbstractAfServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 416073998020317223L;

    protected AfApiClient getAfApiClient() {
        return new AfApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
                AfServletGouvPropertiesResolver.getApiUser(), AfServletGouvPropertiesResolver.getApiPwd());
        
        // Pour quand on sera prêt à appeler la démarche en JWT :
//        return new AfApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
//                AfServletGouvPropertiesResolver.getApiJwt());
    }

}
