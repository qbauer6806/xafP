package mc.gouv.xaf.back.service.itg.ulis;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StreamUtils;

@Slf4j
public class UlisLogInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UlisLogInterceptor.class);

    private final boolean logBody;

    public UlisLogInterceptor() {
        this(true);
    }

    public UlisLogInterceptor(boolean logBody) {
        this.logBody = logBody;
    }

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        if (logBody) {
            log.info("API Call to ULIS - REQUEST body: {}", new String(body, StandardCharsets.UTF_8));
        } else {
            log.info("API Call to ULIS - REQUEST (body masqué)");
        }

        ClientHttpResponse response = execution.execute(request, body);

        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        LOGGER.debug("API Response from ULIS - RESPONSE body: {}", responseBody);

        return response;
    }

    public static BufferingClientHttpRequestFactory requestFactory() {
        return new BufferingClientHttpRequestFactory(new JdkClientHttpRequestFactory());
    }
}
