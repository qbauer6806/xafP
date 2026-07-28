package mc.gouv.xaf.front.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Classe permettant d'appeler GICHUNI afin de récupérer les informations de profil de l'usager.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GichuniService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GichuniService.class);

    private final FrontGouvPropertiesResolver propertiesResolver;

    private URL getURL(Integer id) {
        URL url = null;
        try {
            url = new URL(propertiesResolver.getGichuniUrl() + "/profiles/legacy-ids/" + id);
        } catch (MalformedURLException e) {
            LOGGER.error("Erreur lors de la constitution de l'URL d'appel à GICHUNI");
        }
        LOGGER.debug("URL d'appel : {}", url);
        return url;
    }

    public UsagerInfosDTO getGichuniApiProfileData(UsagerInfosDTO uinfos) {
        URL url = getURL(uinfos.getId());
        if (url == null) {
            LOGGER.error("GichkeyService : url null !");
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url.toString());
        getRequest.setHeader("Connection", "keep-alive");
        getRequest.setHeader("Accept-Encoding", "gzip, deflate, br");
        getRequest.setHeader("Accept", "*/*");
        getRequest.setHeader("Authorization", "Bearer " + uinfos.getTokenInfo().getAccessToken());

        LOGGER.info("Appel à GICHUNI");
        try {
            ClassicHttpResponse getResponse = (ClassicHttpResponse) client.execute(getRequest);
            String resp = IOUtils.toString(getResponse.getEntity().getContent());
            LOGGER.debug("Status : {}, resp = {}", getResponse.getCode(), resp);
            ArrayNode anode = new ObjectMapper().readValue(resp, ArrayNode.class);
            return setupUsagerInfos(anode, uinfos);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'appel à GICHUNI", e);
            return null;
        }
    }

    private UsagerInfosDTO setupUsagerInfos(ArrayNode resp, UsagerInfosDTO uinfos) {
        ObjectNode node = (ObjectNode) resp.get(0);
        JsonNode n = node.get("raisonSociale");
        if (n instanceof StringNode) {
            String raisonSociale = n.asString();
            uinfos.setRaisonSociale(raisonSociale);
        }
        n = node.get("login");
        if (n instanceof StringNode) {
            String login = n.asString();
            uinfos.setLogin(login);
        }
        JsonNode sh = node.get("etat");
        if (sh instanceof IntNode) {
            Short etat = sh.shortValue();
            uinfos.setEtat(etat);
        }
        sh = node.get("titre");
        if (sh instanceof IntNode) {
            Short titre = sh.shortValue();
            uinfos.setTitre(titre);
        }
        n = node.get("paysCode");
        if (n instanceof StringNode) {
            String paysCode = n.asString();
            uinfos.setPaysCode(paysCode);
        }
        n = node.get("adresse1");
        if (n instanceof StringNode) {
            String adresse1 = n.asString();
            uinfos.setAdresse1(adresse1);
        }
        n = node.get("adresse2");
        if (n instanceof StringNode) {
            String adresse2 = n.asString();
            uinfos.setAdresse2(adresse2);
        }
        n = node.get("complementAdresse");
        if (n instanceof StringNode) {
            String complementAdresse = n.asString();
            uinfos.setComplementAdresse(complementAdresse);
        }
        n = node.get("codePostal");
        if (n instanceof StringNode) {
            String codePostal = n.asString();
            uinfos.setCodePostal(codePostal);
        }
        n = node.get("ville");
        if (n instanceof StringNode) {
            String ville = n.asString();
            uinfos.setVille(ville);
        }
        n = node.get("nomPays");
        if (n instanceof StringNode) {
            String nomPays = n.asString();
            uinfos.setNomPays(nomPays);
        }
        return uinfos;
    }

}
