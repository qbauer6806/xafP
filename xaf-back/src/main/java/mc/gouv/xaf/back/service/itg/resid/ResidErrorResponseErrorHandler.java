package mc.gouv.xaf.back.service.itg.resid;


import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.shared.itg.resid.exception.ResidHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
public class ResidErrorResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResidErrorResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return (httpResponse.getStatusCode().series() == CLIENT_ERROR || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR || httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            LOGGER.error("Erreur lors de l'appel à RESID - Erreur {}", httpResponse.getStatusCode());
            throw new ObjectMapper().readValue(httpResponse.getBody(), ResidHttpResponseException.class);
        }
    }
}
