package mc.gouv.xaf.servlet;

import javax.servlet.http.HttpServlet;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

public class AbstractAfServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 416073998020317223L;

    protected AfApiClient getAfApiClient() {
        return AppFactoryServletUtils.getAfApiClient();
    }

}
