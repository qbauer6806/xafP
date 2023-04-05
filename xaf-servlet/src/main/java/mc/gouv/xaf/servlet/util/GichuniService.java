package mc.gouv.xaf.servlet.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Classe permettant d'appeler GICHUNI afin de récupérer les informations de profil de
 * l'usager.
 *
 * @author qdeme
 */
public class GichuniService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GichuniService.class);

    private GichuniService() {
    }

    private static URL getURL(Integer id) {
        URL url = null;
        try {
            url = new URL(AfServletGouvPropertiesResolver.getGichuniUrl() + "/profiles/legacy-ids/" + id);
        } catch (MalformedURLException e) {
            LOGGER.error("Erreur lors de la constitution de l'URL d'appel à GICHUNI");
        }
        LOGGER.info("URL d'appel : {}", url);
        return url;
    }

    public static UsagerInfosDTO getGichuniApiProfileData(UsagerInfosDTO uinfos) {
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
            HttpResponse getResponse = client.execute(getRequest);
            String resp = IOUtils.toString(getResponse.getEntity().getContent());
            LOGGER.info("Status : {}, resp = {}", getResponse.getStatusLine().getStatusCode(), resp);
            ArrayNode anode = new ObjectMapper().readValue(resp, ArrayNode.class);
            return setupUsagerInfos(anode, uinfos);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'appel à GICHUNI", e);
            return null;
        }
    }

    private static UsagerInfosDTO setupUsagerInfos(ArrayNode resp, UsagerInfosDTO uinfos) {
        ObjectNode node = (ObjectNode) resp.get(0);
        JsonNode n = node.get("raisonSociale");
        if (n instanceof TextNode) {
            String raisonSociale = n.asText();
            uinfos.setRaisonSociale(raisonSociale);
        }
        n = node.get("login");
        if (n instanceof TextNode) {
            String login = n.asText();
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
        if (n instanceof TextNode) {
            String paysCode = n.asText();
            uinfos.setPaysCode(paysCode);
        }
        n = node.get("adresse1");
        if (n instanceof TextNode) {
            String adresse1 = n.asText();
            uinfos.setAdresse1(adresse1);
        }
        n = node.get("adresse2");
        if (n instanceof TextNode) {
            String adresse2 = n.asText();
            uinfos.setAdresse2(adresse2);
        }
        n = node.get("complementAdresse");
        if (n instanceof TextNode) {
            String complementAdresse = n.asText();
            uinfos.setComplementAdresse(complementAdresse);
        }
        n = node.get("codePostal");
        if (n instanceof TextNode) {
            String codePostal = n.asText();
            uinfos.setCodePostal(codePostal);
        }
        n = node.get("ville");
        if (n instanceof TextNode) {
            String ville = n.asText();
            uinfos.setVille(ville);
        }
        n = node.get("nomPays");
        if (n instanceof TextNode) {
            String nomPays = n.asText();
            uinfos.setNomPays(nomPays);
        }
        return uinfos;
    }

}
