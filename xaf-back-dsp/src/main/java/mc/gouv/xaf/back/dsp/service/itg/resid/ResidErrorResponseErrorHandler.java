package mc.gouv.xaf.back.dsp.service.itg.resid;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
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
        HttpStatus.Series series = HttpStatus.Series.valueOf(httpResponse.getStatusCode().value());
        return CLIENT_ERROR.equals(series) || SERVER_ERROR.equals(series);
    }

    @Override
    public void handleError(@NotNull ClientHttpResponse httpResponse) throws IOException {
        LOGGER.error("Erreur lors de l'appel à RESID - Erreur {}", httpResponse.getStatusCode());
        ResidHttpResponseException ex = new ObjectMapper().readValue(httpResponse.getBody(),
                ResidHttpResponseException.class);
        ex.setHttpStatus(httpResponse.getStatusCode().value());
        throw ex;
    }
}
