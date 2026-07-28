package mc.gouv.xaf.apiclient.mail;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.shared.dto.mail.MailDTO;
import mc.gouv.xaf.shared.dto.mail.MailSentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class MailClient extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    @Value("${mc.gouv.appli.shared.backapi.mail.enabled:true}")
    private boolean mailEnabled;

    public MailClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken));
    }

    public MailSentDTO sendEmail(MailDTO mailDTO, Map<String, InputStream> attachments) {
        LOGGER.debug("sendEmail({},{})", mailDTO, attachments);

        boolean pj = attachments != null && !attachments.isEmpty();

        if (!pj) {
            LOGGER.debug("Appel ({}, pj=false)...", getServiceUrl());
            if (mailEnabled) {
                return getRestClient().post().contentType(MediaType.APPLICATION_JSON).body(mailDTO).retrieve()
                        .body(MailSentDTO.class);
            }
            return new MailSentDTO();
        }

        LOGGER.debug("Appel ({}, pj=true)...", getServiceUrl());

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("email", mailDTO);

        attachments.forEach((filename, inputStream) -> {
            LOGGER.info("Ajout d'une pièce jointe : {}", filename);

            parts.add("file", new InputStreamResource(inputStream) {

                @Override
                public String getFilename() {
                    return filename;
                }
            });
        });

        return getRestClient().post().contentType(MediaType.MULTIPART_FORM_DATA).body(parts).retrieve()
                .body(MailSentDTO.class);
    }

    public MailSentDTO sendEmail(MailDTO mailDTO) {
        LOGGER.debug("sendEmail({})", mailDTO);
        return sendEmail(mailDTO, null);
    }

    public MailDTO getEmail(Integer id) {
        LOGGER.info("getEmail({})", id);

        return getRestClient().get().uri("/{id}", id).retrieve().body(MailDTO.class);
    }

    public List<MailDTO> getEmails(String metaKey, String metaValue) {
        LOGGER.info("getEmails({},{})", metaKey, metaValue);

        return getRestClient().get().uri(uriBuilder -> uriBuilder.path("/").queryParam("metaKey", metaKey)
                        .queryParam("metaValue", metaValue).build()).retrieve()
                .body(new ParameterizedTypeReference<List<MailDTO>>() {

                });
    }
}
