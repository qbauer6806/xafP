package mc.gouv.xaf.back.dsp.service.itg.resid;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class ResidErrorResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResidErrorResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        return (httpResponse.getStatusCode().value() == CLIENT_ERROR.value() || httpResponse.getStatusCode().value() == SERVER_ERROR.value());
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().value() == HttpStatus.Series.SERVER_ERROR.value()|| httpResponse.getStatusCode().value() == HttpStatus.Series.CLIENT_ERROR.value()) {
            LOGGER.error("Erreur lors de l'appel à RESID - Erreur {}", httpResponse.getStatusCode());
            ResidHttpResponseException ex = new ObjectMapper().readValue(httpResponse.getBody(), ResidHttpResponseException.class);
            ex.setHttpStatus(httpResponse.getStatusCode().value());
            throw ex;
        }
    }
}
