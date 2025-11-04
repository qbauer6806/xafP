package mc.gouv.xaf.back.paiement.service.itg.cir.service.impl;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logEndMethod;
import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
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
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    public PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande)
            throws HttpResponseException {
        logStartMethod(LOGGER);
        try (Client client = ClientBuilder.newClient()) {
            String serviceUrl = paiementPropertiesResolver.getFactureUrl();
            WebTarget targetGetPermis = client.target(serviceUrl + PERMIS_ROUTE);
            try {
                logStartMethod(LOGGER);
                LOGGER.info("Parameters [ getPermis {}] ", numPermis);
                Response response = targetGetPermis.resolveTemplate("numPermis", numPermis).request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .get();
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), response.toString());
                }

                PermisDTO permisDTO = response.readEntity(PermisDTO.class);
                logEndMethod(LOGGER);
                return permisDTO;
            } catch (HttpResponseException e) {
                // On envoie le mail d'incident qu'en cas d'erreur au niveau du serveur CIR
                if (Response.Status.Family.familyOf(e.getStatusCode()) == Response.Status.Family.SERVER_ERROR) {
                    sendMailProblemeCir(pkDemande, identifiantDemande, e.getMessage());
                }
                throw e;
            }
        }

    }

    @Override
    public RegistreDTO getRegistre(Integer registre, int pkDemande, String identifiantDemande)
            throws HttpResponseException {
        logStartMethod(LOGGER);
        try (Client client = ClientBuilder.newClient()) {
            String serviceUrl = paiementPropertiesResolver.getFactureUrl();
            WebTarget targetGetVehicules = client.target(serviceUrl + VEHICULES_ROUTE);
            try {
                logStartMethod(LOGGER);
                LOGGER.info("Parameters [ getVehicule {}] ", registre);
                Response response = targetGetVehicules.queryParam("registre", registre, "inactif", false).request()
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + paiementPropertiesResolver.getFactureToken())
                        .get();
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new HttpResponseException(response.getStatus(), response.toString());
                }

                RegistreDTO registreDTO = response.readEntity(RegistreDTO.class);
                logEndMethod(LOGGER);
                return registreDTO;
            } catch (HttpResponseException e) {
                // On envoie le mail d'incident qu'en cas d'erreur au niveau du serveur CIR
                if (Response.Status.Family.familyOf(e.getStatusCode()) == Response.Status.Family.SERVER_ERROR) {
                    sendMailProblemeCir(pkDemande, identifiantDemande, e.getMessage());
                }
                throw e;
            }
        }
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
