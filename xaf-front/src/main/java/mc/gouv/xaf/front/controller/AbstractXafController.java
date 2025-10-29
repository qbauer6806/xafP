package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.apiclient.paiement.monetico.MoneticoApiClient;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xapi.error.exception.WebException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

/**
 * @author mpavone
 */
@Controller
public class AbstractXafController {

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    protected AfApiClient getAfApiClient() {
        return xafFrontserverUtils.getAfApiClient();
    }

    protected int getCodeErreur(Exception exception) {
        return exception instanceof WebException webException
                ? webException.getHttpStatus()
                : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
