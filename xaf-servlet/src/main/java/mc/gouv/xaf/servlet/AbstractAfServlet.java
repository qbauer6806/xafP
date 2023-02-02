package mc.gouv.xaf.servlet;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xapi.error.exception.WebException;
import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServlet;

/**
 * 
 * @author qdeme
 * 
 */
public class AbstractAfServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 416073998020317223L;

    protected AfApiClient getAfApiClient() {
        return AppFactoryServletUtils.getAfApiClient();
    }

    protected int getCodeErreur(Exception exception){
        return exception instanceof WebException ? ((WebException) exception).getHttpStatus() :
                HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

}
