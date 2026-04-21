package mc.gouv.xaf.back.service.itg.ulis;

import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class UlisLogFilter implements WriterInterceptor, ReaderInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UlisLogFilter.class);

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        OutputStream originalStream = context.getOutputStream();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        context.setOutputStream(baos);
        try {
            context.proceed();
        } finally {
            baos.writeTo(originalStream);
            baos.close();
            context.setOutputStream(originalStream);
            LOGGER.info("API Call to ULIS - REQUEST body: {}", baos.toString(StandardCharsets.UTF_8));
        }
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
        InputStream is = context.getInputStream();
        String body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        LOGGER.debug("API Response from ULIS - RESPONSE body: {}", body);

        context.setInputStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        return context.proceed();
    }
}
