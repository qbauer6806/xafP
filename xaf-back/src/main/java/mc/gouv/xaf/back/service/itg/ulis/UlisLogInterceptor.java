package mc.gouv.xaf.back.service.itg.ulis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;

public class UlisLogInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UlisLogInterceptor.class);

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        LOGGER.info("API Call to ULIS - REQUEST body: {}", new String(body, StandardCharsets.UTF_8));

        ClientHttpResponse response = execution.execute(request, body);

        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        LOGGER.debug("API Response from ULIS - RESPONSE body: {}", responseBody);

        return response;
    }

    public static BufferingClientHttpRequestFactory requestFactory() {
        return new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
    }
}
