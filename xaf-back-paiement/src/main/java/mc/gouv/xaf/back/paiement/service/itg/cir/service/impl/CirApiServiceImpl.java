package mc.gouv.xaf.back.paiement.service.itg.cir.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.dto.itg.cir.RegistreDTO;
import mc.gouv.xaf.back.paiement.properties.PaiementPropertiesResolver;
import mc.gouv.xaf.back.paiement.service.itg.cir.service.CirApiService;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CirApiServiceImpl implements CirApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CirApiServiceImpl.class);

    public static final String PERMIS_ROUTE = "v1/permis/{numPermis}";
    public static final String VEHICULES_ROUTE = "v1/vehicules";
    public static final String BEARER_PREFIX = "Bearer ";

    private final MailService mailService;
    private final PaiementPropertiesResolver paiementPropertiesResolver;

    @Override
    public PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande) {

        logStartMethod(LOGGER);

        try {
            LOGGER.info("Parameters [ getPermis {}] ", numPermis);

            PermisDTO permisDTO = getRestClient().get().uri(PERMIS_ROUTE, numPermis).exchange((request, response) -> {
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new ResponseStatusException(response.getStatusCode(), response.toString());
                }

                return response.bodyTo(PermisDTO.class);
            });

            logEndMethod(LOGGER);
            return permisDTO;

        } catch (ResponseStatusException e) {
            // On envoie le mail d'incident qu'en cas d'erreur au niveau du serveur CIR
            if (e.getStatusCode().is5xxServerError()) {
                sendMailProblemeCir(pkDemande, identifiantDemande, e.getReason());
            }
            throw e;
        }
    }

    @Override
    public RegistreDTO getRegistre(Integer registre, int pkDemande, String identifiantDemande) {

        logStartMethod(LOGGER);

        try {
            LOGGER.info("Parameters [ getVehicule {}] ", registre);

            RegistreDTO registreDTO = getRestClient().get()
                    .uri(uriBuilder -> uriBuilder.path(VEHICULES_ROUTE).queryParam("registre", registre)
                            .queryParam("inactif", false).build()).exchange((request, response) -> {
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            throw new ResponseStatusException(response.getStatusCode(), response.toString());
                        }

                        return response.bodyTo(RegistreDTO.class);
                    });

            logEndMethod(LOGGER);
            return registreDTO;

        } catch (ResponseStatusException e) {
            // On envoie le mail d'incident qu'en cas d'erreur au niveau du serveur CIR
            if (e.getStatusCode().is5xxServerError()) {
                sendMailProblemeCir(pkDemande, identifiantDemande, e.getReason());
            }
            throw e;
        }
    }

    private RestClient getRestClient() {
        return RestClient.builder().baseUrl(paiementPropertiesResolver.getFactureUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                .build();
    }

    private void sendMailProblemeCir(int pkDemande, String identifiant, String reponse) {
        String subjectTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_CORPS";

        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name());

        Map<String, Object> model = new HashMap<>();
        model.put("reponseApi", reponse);

        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, pkDemande, identifiant, 5, model,
                null);
    }
}
