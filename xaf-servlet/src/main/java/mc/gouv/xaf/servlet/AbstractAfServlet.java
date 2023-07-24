package mc.gouv.xaf.servlet;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xapi.error.exception.WebException;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * @author qdeme
 * 
 */
public class AbstractAfServlet extends HttpServlet {

    private static final long serialVersionUID = 416073998020317223L;

    protected AfApiClient getAfApiClient() {
        return AppFactoryServletUtils.getAfApiClient();
    }

    protected int getCodeErreur(Exception exception){
        return exception instanceof WebException ? ((WebException) exception).getHttpStatus() :
                HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(!req.getMethod().equals("PATCH")) {
            super.service(req, resp);
        } else {
            this.doPatch(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int errno = req.getProtocol().endsWith("1.1") ? 405 : 400;

        resp.sendError(errno, "Method PATCH not allowed");
    }
}
