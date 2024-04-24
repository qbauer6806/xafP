package mc.gouv.xaf.servlet.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mc.gouv.xaf.servlet.dto.KeycloakTokenInfo;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.UsagerTypeEnum;

/**
 * Classe permettant d'appeler GICHKEY afin de gérer le login/logout de l'usager, le rafraîchissement des tokens, etc.
 *
 * @author qdeme
 */
public class GichkeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GichkeyService.class);

    private static final String NULL_URL = "GichkeyService : url null !";
    private static final String APPEL_GICHKEY = "Appel à GICHKEY";
    private static final String ERREUR_GICHKEY = "Erreur lors de l'appel à GICHKEY";

    private static final String KEEP_ALIVE = "keep-alive";
    private static final String ENCODING = "gzip, deflate, br";
    private static final String ACCEPT = "*/*";

    private GichkeyService() {
    }

    private static URL getURL(String chemin) {
        URL url = null;
        try {
            url = new URL(AfServletGouvPropertiesResolver.getGichkeyUrl() + chemin);
        } catch (MalformedURLException e) {
            LOGGER.error("Erreur lors de la constitution de l'URL d'appel à GICHKEY", e);
        }
        LOGGER.info("URL d'appel : {}", url);
        return url;
    }

    public static KeycloakTokenInfo getTokenFromAuthCode(String code) {
        URL url = getURL("/protocol/openid-connect/token");
        if (url == null) {
            LOGGER.error(NULL_URL);
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost(url.toString());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("code", code));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientSecret()));
        nvps.add(new BasicNameValuePair("redirect_uri",
                AfServletGouvPropertiesResolver.getGichkeyKeycloakRedirectUri()));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair(RequestConstant.SCOPE_PARAM, "openid mconnect monguichet"));

        postRequest.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        postRequest.setHeader(HttpHeaders.CONNECTION, KEEP_ALIVE);
        postRequest.setHeader(HttpHeaders.ACCEPT_ENCODING, ENCODING);
        postRequest.setHeader(HttpHeaders.ACCEPT, ACCEPT);

        LOGGER.info("Paramètres de la requête : {}", nvps);

        LOGGER.info(APPEL_GICHKEY);
        try {
            HttpResponse postResponse = client.execute(postRequest);
            LOGGER.info("resp : {}", postResponse.getStatusLine().getStatusCode());
            String resp = IOUtils.toString(postResponse.getEntity().getContent());
            LOGGER.info("resp = {}", resp);

            KeycloakTokenInfo tokenInfo = jsonToTokenInfo(resp);

            LOGGER.info("Access token: {}", tokenInfo.getAccessToken());
            LOGGER.info("Refresh token: {}", tokenInfo.getRefreshToken());

            return tokenInfo;
        } catch (IOException e) {
            LOGGER.error(ERREUR_GICHKEY, e);
            return null;
        }
    }

    private static KeycloakTokenInfo jsonToTokenInfo(String resp) throws IOException {
        KeycloakTokenInfo tokenInfo = new KeycloakTokenInfo();
        ObjectNode node = new ObjectMapper().readValue(resp, ObjectNode.class);
        TextNode n = (TextNode) node.get("access_token");
        String accessToken = n.asText();
        tokenInfo.setAccessToken(accessToken);
        n = (TextNode) node.get(RequestConstant.REFRESH_TOKEN_PARAM);
        String refreshToken = n.asText();
        tokenInfo.setRefreshToken(refreshToken);
        IntNode in = (IntNode) node.get("expires_in");
        Integer expiresIn = in.asInt();
        tokenInfo.setExpiresIn(expiresIn);
        in = (IntNode) node.get("refresh_expires_in");
        Integer refreshExpiresIn = in.asInt();
        tokenInfo.setRefreshExpiresIn(refreshExpiresIn);
        n = (TextNode) node.get("token_type");
        String tokenType = n.asText();
        tokenInfo.setTokenType(tokenType);
        in = (IntNode) node.get("not-before-policy");
        Integer notBeforePolicy = in.asInt();
        tokenInfo.setNotBeforePolicy(notBeforePolicy);
        n = (TextNode) node.get("session_state");
        String sessionState = n.asText();
        tokenInfo.setSessionState(sessionState);
        n = (TextNode) node.get(RequestConstant.SCOPE_PARAM);
        String scope = n.asText();
        tokenInfo.setScope(scope);
        return tokenInfo;
    }

    public static UsagerInfosDTO getUsagerInfosFromToken(KeycloakTokenInfo tokenInfo) {
        String[] chunks = tokenInfo.getAccessToken().split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        LOGGER.info("payload={}", payload);

        ObjectNode node = null;
        try {
            node = new ObjectMapper().readValue(payload, ObjectNode.class);
        } catch (IOException e) {
            LOGGER.error("Erreur lors du new ObjectMapper().readValue()", e);
        }
        if (node == null) {
            LOGGER.error("getUsagerInfosFromToken : node == null !");
            return null;
        }
        TextNode nameNode = (TextNode) node.get("family_name");
        TextNode givenNameNode = (TextNode) node.get("given_name");
        TextNode emailNode = (TextNode) node.get("email");
        IntNode usagerIdNode = (IntNode) node.get("usager_id");
        TextNode typeNode = (TextNode) node.get("type");
        String usagerNom = nameNode.asText();
        String usagerPrenom = givenNameNode.asText();
        String usagerEmail = emailNode.asText();
        Integer usagerId = usagerIdNode.asInt();
        String type = null;
        if (typeNode != null) {
            type = typeNode.asText();
        }

        LOGGER.info("Usager : {} {} ({})", usagerPrenom, usagerNom, usagerEmail);

        UsagerInfosDTO uinfos = new UsagerInfosDTO();
        uinfos.setEmail(usagerEmail);
        uinfos.setId(usagerId);
        uinfos.setLogin(StringUtils.defaultString(usagerPrenom + " " + usagerNom));
        uinfos.setNom(usagerNom);
        uinfos.setPrenom(usagerPrenom);
        uinfos.setUsagerCourrier(false);
        if (type != null) {
            uinfos.setType(UsagerTypeEnum.valueOf(type.toUpperCase()));
        }

        uinfos.setTokenInfo(tokenInfo);

        // Récupération des informations MConnect si elles sont présentes
        JsonNode mconnect = node.get("mconnect-identity");
        if (mconnect != null && !(mconnect instanceof NullNode)) {
            try {

                TextNode givenNameNode0 = (TextNode) mconnect.get("given_name");
                TextNode familyNameNode = (TextNode) mconnect.get("family_name");
                TextNode birthNameNode = (TextNode) mconnect.get("birth_name");
                TextNode genderNode = (TextNode) mconnect.get("gender");
                TextNode birthPlaceNode = (TextNode) mconnect.get("birth_place");
                TextNode birthDatetimeNode = (TextNode) mconnect.get("birth_datetime");
                TextNode authorityNode = (TextNode) mconnect.get("authority");
                TextNode birthPlaceCountryNode = (TextNode) mconnect.get("birth_place_country");
                TextNode birthPlaceCityNode = (TextNode) mconnect.get("birth_place_city");
                DonneesMConnectDTO mConnectUInfos = new DonneesMConnectDTO();
                mConnectUInfos.setGivenName(givenNameNode0.asText());
                mConnectUInfos.setFamilyName(familyNameNode.asText());
                mConnectUInfos.setBirthName(birthNameNode.asText());
                mConnectUInfos.setGender(genderNode.asText());
                mConnectUInfos.setBirthPlace(birthPlaceNode.asText());
                mConnectUInfos
                        .setBirthDatetime(new SimpleDateFormat("yyyyMMddHHmmss").parse(birthDatetimeNode.asText()));
                mConnectUInfos.setAuthority(authorityNode.asText());
                mConnectUInfos.setBirthPlaceCountry(birthPlaceCountryNode.asText());
                mConnectUInfos.setBirthPlaceCity(birthPlaceCityNode.asText());
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode donneesExternes = mapper.createObjectNode();
                donneesExternes.put("mconnect", mapper.valueToTree(mConnectUInfos));
                uinfos.setDonneesExternes(donneesExternes);
                LOGGER.info("Informations MConnect disponibles : {}", mConnectUInfos);
                uinfos.setmConnect(true);
                // Mettre login à "" si usager MConnect
                uinfos.setLogin("");
            } catch (ParseException e) {
                LOGGER.error("Erreur lors du parsing des informations certifiées MConnect", e);
            }
        }
        return uinfos;
    }

    public static HttpResponse logout(UsagerInfosDTO uinfos) {
        URL url = getURL("/protocol/openid-connect/logout");
        if (url == null) {
            LOGGER.error(NULL_URL);
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost(url.toString());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientSecret()));
        nvps.add(new BasicNameValuePair(RequestConstant.REFRESH_TOKEN_PARAM, uinfos.getTokenInfo().getRefreshToken()));

        postRequest.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        postRequest.setHeader(HttpHeaders.CONNECTION, KEEP_ALIVE);
        postRequest.setHeader(HttpHeaders.ACCEPT_ENCODING, ENCODING);
        postRequest.setHeader(HttpHeaders.ACCEPT, ACCEPT);
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + uinfos.getTokenInfo().getAccessToken());

        LOGGER.info(APPEL_GICHKEY);
        try {
            return client.execute(postRequest);
        } catch (IOException e) {
            LOGGER.error(ERREUR_GICHKEY, e);
        }
        return null;
    }

    public static UsagerInfosDTO checkTokens(UsagerInfosDTO usagerInfosDTO, boolean forceRefresh) {

        LOGGER.info("==================== KeycloakService.checkTokens() ...");
        UsagerInfosDTO ret;
        Date derniereObtention = usagerInfosDTO.getTokenInfo().getDateObtention();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(derniereObtention);
        // On prend la durée d'expiration moins 30 secondes pour avoir une marge
        calendar.add(Calendar.SECOND, usagerInfosDTO.getTokenInfo().getExpiresIn() - 30);
        Date expiration = calendar.getTime();
        Date now = new Date();
        LOGGER.info("Dernière obtention = {}, expiration = {}, date courante = {}", derniereObtention, expiration, now);
        if (now.after(expiration) || forceRefresh) {
            LOGGER.info("Il faut rafraîchir les tokens");

            // On n'oublie pas de réincorporer l'accessId s'il était présent
            Integer accessId = usagerInfosDTO.getAccessId();
            // On refresh les tokens
            KeycloakTokenInfo tokenInfo = refreshTokens(usagerInfosDTO.getTokenInfo());
            if (tokenInfo == null) {
                return null;
            }
            // On refresh les infos usagers extraites de l'accessToken
            ret = getUsagerInfosFromToken(tokenInfo);
            if (ret != null) {
                // Appel à GICHUNI pour obtenir des informations de profil complémentaires
                ret = GichuniService.getGichuniApiProfileData(ret);
                ret.setAccessId(accessId);
            }
        } else {
            LOGGER.info("Pas besoin de rafraîchir les tokens");
            ret = usagerInfosDTO;
        }

        LOGGER.info("==================== FIN KeycloakService.checkTokens()");
        return ret;
    }

    public static KeycloakTokenInfo refreshTokens(KeycloakTokenInfo tokenInfo) {
        URL url = getURL("/protocol/openid-connect/token");
        if (url == null) {
            LOGGER.error(NULL_URL);
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost(url.toString());

        LOGGER.info("refreshToken utilisé pour appel : {}", tokenInfo.getRefreshToken());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(RequestConstant.REFRESH_TOKEN_PARAM, tokenInfo.getRefreshToken()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                AfServletGouvPropertiesResolver.getGichkeyClientSecret()));
        nvps.add(new BasicNameValuePair("grant_type", RequestConstant.REFRESH_TOKEN_PARAM));
        nvps.add(new BasicNameValuePair(RequestConstant.SCOPE_PARAM, "openid mconnect monguichet"));

        postRequest.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        postRequest.setHeader(HttpHeaders.CONNECTION, KEEP_ALIVE);
        postRequest.setHeader(HttpHeaders.ACCEPT_ENCODING, ENCODING);
        postRequest.setHeader(HttpHeaders.ACCEPT, ACCEPT);

        LOGGER.info(APPEL_GICHKEY);
        try {
            HttpResponse postResponse = client.execute(postRequest);
            int statusCode = postResponse.getStatusLine().getStatusCode();
            String resp = IOUtils.toString(postResponse.getEntity().getContent());
            if (statusCode != 200) {
                LOGGER.error("Erreur lors de l'appel à GICHKEY : {}", resp);
                return null;
            }
            LOGGER.info("Status code = {}, resp = {}", statusCode, resp);

            tokenInfo = jsonToTokenInfo(resp);
            tokenInfo.setDateObtention(new Date());

            LOGGER.info("Nouvel accessToken={}", tokenInfo.getAccessToken());
            LOGGER.info("Nouveau refreshToken={}", tokenInfo.getRefreshToken());

            LOGGER.info("==================== FIN KeycloakService.checkTokens()");
            return tokenInfo;
        } catch (IOException e) {
            LOGGER.error(ERREUR_GICHKEY, e);
        }
        return null;
    }

}
