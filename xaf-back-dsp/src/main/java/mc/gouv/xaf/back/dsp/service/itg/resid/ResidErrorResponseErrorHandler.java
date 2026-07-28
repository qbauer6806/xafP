package mc.gouv.xaf.back.dsp.service.itg.resid;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

import java.io.IOException;
import java.net.URI;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import tools.jackson.databind.ObjectMapper;

@Component
public class ResidErrorResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResidErrorResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
        HttpStatus.Series series = HttpStatus.Series.valueOf(httpResponse.getStatusCode().value());
        return CLIENT_ERROR.equals(series) || SERVER_ERROR.equals(series);
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse httpResponse) throws IOException {
        LOGGER.error("Erreur lors de l'appel à RESID - Erreur {}", httpResponse.getStatusCode());
        ResidHttpResponseException ex = new ObjectMapper().readValue(httpResponse.getBody(),
                ResidHttpResponseException.class);
        ex.setHttpStatus(httpResponse.getStatusCode().value());
        throw ex;
    }
}
