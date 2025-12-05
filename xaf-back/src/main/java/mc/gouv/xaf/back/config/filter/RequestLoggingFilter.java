package mc.gouv.xaf.back.config.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"), MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
        }
    }

    protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            beforeRequest(request);
            filterChain.doFilter(request, response);
        } finally {
            afterRequest(request, response);
            response.copyBodyToResponse();
        }
    }

    protected void beforeRequest(ContentCachingRequestWrapper request) {
        if (LOGGER.isDebugEnabled()) {
            logRequestHeader(request, request.getRemoteAddr() + "|>");
        }
    }

    protected void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (LOGGER.isDebugEnabled()) {
            logRequestBody(request, request.getRemoteAddr() + "|>");
            logResponse(response, request.getRemoteAddr() + "|<");
        }
    }

    private static void logRequestHeader(ContentCachingRequestWrapper request, String prefix) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            LOGGER.info("{} {} {}", prefix, request.getMethod(), request.getRequestURI());
        } else {
            LOGGER.info("{} {} {}?{}", prefix, request.getMethod(), request.getRequestURI(), queryString);
        }

        for (String headerName : Collections.list(request.getHeaderNames())) {
            for (String headerValue : Collections.list(request.getHeaders(headerName))) {
                if (StringUtils.equalsIgnoreCase(headerName, "Authorization")) {
                    headerValue = "*****";
                }
                LOGGER.info("{} {}: {}", prefix, headerName, headerValue);
            }
        }
        LOGGER.info("{}", prefix);
    }

    private static void logRequestBody(ContentCachingRequestWrapper request, String prefix) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content, request.getContentType(), request.getCharacterEncoding(), prefix);
        }
    }

    private static void logResponse(ContentCachingResponseWrapper response, String prefix) {
        int status = response.getStatus();
        LOGGER.info("{} {} {}", prefix, status, HttpStatus.valueOf(status).getReasonPhrase());

        for (String headerName : response.getHeaderNames()) {
            for (String headerValue : response.getHeaders(headerName)) {
                LOGGER.info("{} {}: {}", prefix, headerName, headerValue);
            }
        }

        LOGGER.info("{}", prefix);
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            logContent(content, response.getContentType(), response.getCharacterEncoding(), prefix);
        }
    }

    private static void logContent(byte[] content, String contentType, String contentEncoding, String prefix) {
        MediaType mediaType = MediaType.valueOf(contentType);
        boolean visible = false;
        for (MediaType visibleMediaType : VISIBLE_TYPES) {
            if (visibleMediaType.includes(mediaType)) {
                visible = true;
                break;
            }
        }

        if (visible) {
            try {
                String contentString = new String(content, contentEncoding);
                for (String line : contentString.split("\r\n|\r|\n")) {
                    LOGGER.info("{} {}", prefix, AfBackUtils.logSafe(line));
                }
            } catch (UnsupportedEncodingException e) {
                LOGGER.info("{} [{} bytes content]", prefix, content.length);
            }
        } else {
            LOGGER.info("{} [{} bytes content]", prefix, content.length);
        }
    }

    private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper contentCachingRequestWrapper) {
            return contentCachingRequestWrapper;
        } else {
            return new ContentCachingRequestWrapper(request);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper contentCachingResponseWrapper) {
            return contentCachingResponseWrapper;
        } else {
            return new ContentCachingResponseWrapper(response);
        }
    }
}
