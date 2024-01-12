package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xapi.error.exception.WebException;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mpavone
 */
@Component
public class AbstractXafController {

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    protected AfApiClient getAfApiClient() {
        return xafFrontserverUtils.getAfApiClient();
    }

    protected int getCodeErreur(Exception exception) {
        return exception instanceof WebException ? ((WebException) exception).getHttpStatus() :
                HttpStatus.INTERNAL_SERVER_ERROR_500;
    }

}
