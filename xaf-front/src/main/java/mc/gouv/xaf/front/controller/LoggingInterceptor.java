package mc.gouv.xaf.front.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
@Profile("stdout")
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().remove("Content-Length");
        //request.getHeaders().add("Content-Length", String.valueOf(body.length));
        request.getHeaders().forEach((k, v) -> System.out.println(k + " : " + v));
        System.out.println("Taille du fichier envoyé : " + body.length + " octets");
        return execution.execute(request, body);
    }
}