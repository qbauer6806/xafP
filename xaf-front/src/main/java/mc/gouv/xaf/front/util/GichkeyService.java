package mc.gouv.xaf.front.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.dto.KeycloakTokenInfo;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.AdresseFacturationDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.TelephoneDTO;
import mc.gouv.xaf.shared.enums.UsagerTypeEnum;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/**
 * Classe permettant d'appeler GICHKEY afin de gérer le login/logout de l'usager, le rafraîchissement des tokens, etc.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class GichkeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GichkeyService.class);

    private static final String NULL_URL = "GichkeyService : url null !";
    private static final String APPEL_GICHKEY = "Appel à GICHKEY";
    private static final String ERREUR_GICHKEY = "Erreur lors de l'appel à GICHKEY";

    private static final String KEEP_ALIVE = "keep-alive";
    private static final String ENCODING = "gzip, deflate, br";
    private static final String ACCEPT = "*/*";

    private final FrontGouvPropertiesResolver propertiesResolver;
    private final GichuniService gichuniService;

    private URL getURL(String chemin) {
        URL url = null;
        try {
            url = new URL(propertiesResolver.getGichkeyUrl() + chemin);
        } catch (MalformedURLException e) {
            LOGGER.error("Erreur lors de la constitution de l'URL d'appel à GICHKEY", e);
        }
        LOGGER.debug("URL d'appel : {}", url);
        return url;
    }

    public KeycloakTokenInfo getTokenFromAuthCode(String code) {
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
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM, propertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                propertiesResolver.getGichkeyClientSecret()));
        nvps.add(new BasicNameValuePair("redirect_uri", propertiesResolver.getGichkeyKeycloakRedirectUrl()));
        nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
        nvps.add(new BasicNameValuePair(RequestConstant.SCOPE_PARAM, "openid mconnect monguichet"));

        postRequest.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        postRequest.setHeader(HttpHeaders.CONNECTION, KEEP_ALIVE);
        postRequest.setHeader(HttpHeaders.ACCEPT_ENCODING, ENCODING);
        postRequest.setHeader(HttpHeaders.ACCEPT, ACCEPT);

        LOGGER.debug("Paramètres de la requête : {}", nvps);

        LOGGER.info(APPEL_GICHKEY);
        try {
            ClassicHttpResponse postResponse = (ClassicHttpResponse) client.execute(postRequest);
            LOGGER.debug("resp : {}", postResponse.getCode());
            String resp = IOUtils.toString(postResponse.getEntity().getContent());
            LOGGER.debug("resp = {}", resp);

            KeycloakTokenInfo tokenInfo = jsonToTokenInfo(resp);

            LOGGER.debug("Access token: {}", tokenInfo.getAccessToken());
            LOGGER.debug("Refresh token: {}", tokenInfo.getRefreshToken());

            return tokenInfo;
        } catch (IOException e) {
            LOGGER.error(ERREUR_GICHKEY, e);
            return null;
        }
    }

    private KeycloakTokenInfo jsonToTokenInfo(String resp) throws IOException {
        KeycloakTokenInfo tokenInfo = new KeycloakTokenInfo();
        ObjectNode node = new ObjectMapper().readValue(resp, ObjectNode.class);
        StringNode n = (StringNode) node.get("access_token");
        String accessToken = n.asString();
        tokenInfo.setAccessToken(accessToken);
        n = (StringNode) node.get(RequestConstant.REFRESH_TOKEN_PARAM);
        String refreshToken = n.asString();
        tokenInfo.setRefreshToken(refreshToken);
        IntNode in = (IntNode) node.get("expires_in");
        Integer expiresIn = in.asInt();
        tokenInfo.setExpiresIn(expiresIn);
        in = (IntNode) node.get("refresh_expires_in");
        Integer refreshExpiresIn = in.asInt();
        tokenInfo.setRefreshExpiresIn(refreshExpiresIn);
        n = (StringNode) node.get("token_type");
        String tokenType = n.asString();
        tokenInfo.setTokenType(tokenType);
        in = (IntNode) node.get("not-before-policy");
        Integer notBeforePolicy = in.asInt();
        tokenInfo.setNotBeforePolicy(notBeforePolicy);
        n = (StringNode) node.get("session_state");
        String sessionState = n.asString();
        tokenInfo.setSessionState(sessionState);
        n = (StringNode) node.get(RequestConstant.SCOPE_PARAM);
        String scope = n.asString();
        tokenInfo.setScope(scope);
        return tokenInfo;
    }

    public UsagerInfosDTO getUsagerInfosFromToken(KeycloakTokenInfo tokenInfo) {
        String[] chunks = tokenInfo.getAccessToken().split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        LOGGER.debug("payload={}", payload);

        ObjectNode node = new ObjectMapper().readValue(payload, ObjectNode.class);
        if (node == null) {
            LOGGER.error("getUsagerInfosFromToken : node == null !");
            return null;
        }
        StringNode nameNode = (StringNode) node.get("family_name");
        StringNode givenNameNode = (StringNode) node.get("given_name");
        StringNode secondNameNode = (StringNode) node.get("secondName");
        StringNode thirdNameNode = (StringNode) node.get("thirdName");
        StringNode nationalityNode = (StringNode) node.get("nationality");
        StringNode birthNameNode = (StringNode) node.get("birth_name");
        StringNode birthCityNode = (StringNode) node.get("birth_city");
        StringNode birthDateNode = (StringNode) node.get("birth_date");
        StringNode phoneNumberNode = (StringNode) node.get("phone_number");
        StringNode landlineNumberNode = (StringNode) node.get("landline_number");
        StringNode publicFunctionStatusNode = (StringNode) node.get("public_function_status");
        BooleanNode phoneNumberVerifiedNode = (BooleanNode) node.get("phone_number_verified");
        BooleanNode emailVerifiedNode = (BooleanNode) node.get("email_verified");
        StringNode emailNode = (StringNode) node.get("email");
        IntNode usagerIdNode = (IntNode) node.get("usager_id");
        StringNode subNode = (StringNode) node.get("sub");
        StringNode typeNode = (StringNode) node.get("type");
        String usagerNom = nameNode.asString();
        String usagerPrenom = givenNameNode.asString();
        String usagerSecondPrenom = secondNameNode != null ? secondNameNode.asString() : null;
        String usagerTroisiemePrenom = thirdNameNode != null ? thirdNameNode.asString() : null;
        String usagerNationalite = nationalityNode != null ? nationalityNode.asString() : null;
        String usagerNomNaissance = birthNameNode != null ? birthNameNode.asString() : null;
        String usagerVilleNaissance = birthCityNode != null ? birthCityNode.asString() : null;
        String usagerTelephonePortable = phoneNumberNode != null ? phoneNumberNode.asString() : null;
        boolean usagerTelephonePortableVerifie = phoneNumberVerifiedNode != null && phoneNumberVerifiedNode.asBoolean();
        boolean emailVerifie = emailVerifiedNode != null && emailVerifiedNode.asBoolean();
        String usagerTelephoneFixe = landlineNumberNode != null ? landlineNumberNode.asString() : null;
        String usagerDateNaissance = birthDateNode != null ? birthDateNode.asString() : null;
        String usagerStatutFonctionPublique =
                publicFunctionStatusNode != null ? publicFunctionStatusNode.asString() : null;
        String usagerEmail = emailNode.asString();
        Integer usagerId = usagerIdNode.asInt();
        String usagerSub = subNode.asString();
        String type = null;
        if (typeNode != null) {
            type = typeNode.asString();
        }

        LOGGER.debug("Usager : {} {} ({})", usagerPrenom, usagerNom, usagerEmail);

        UsagerInfosDTO uinfos = new UsagerInfosDTO();
        uinfos.setEmail(usagerEmail);
        uinfos.setId(usagerId);
        uinfos.setLogin(StringUtils.defaultString(usagerPrenom + " " + usagerNom));
        uinfos.setNom(usagerNom);
        uinfos.setPrenom(usagerPrenom);
        uinfos.setSecondPrenom(usagerSecondPrenom);
        uinfos.setTroisiemePrenom(usagerTroisiemePrenom);
        uinfos.setNomNaissance(usagerNomNaissance);
        uinfos.setVilleNaissance(usagerVilleNaissance);
        if (usagerDateNaissance != null && !usagerDateNaissance.isEmpty()) {
            uinfos.setDateNaissance(LocalDate.parse(usagerDateNaissance, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    .atStartOfDay(ZoneOffset.UTC).toInstant());
        }
        if (usagerTelephonePortable != null && !usagerTelephonePortable.isEmpty()) {
            uinfos.setTelephonePortable(new TelephoneDTO(usagerTelephonePortable));
        }
        if (usagerTelephoneFixe != null && !usagerTelephoneFixe.isEmpty()) {
            uinfos.setTelephoneFixe(new TelephoneDTO(usagerTelephoneFixe));
        }
        uinfos.setTelephonePortableVerifie(usagerTelephonePortableVerifie);
        uinfos.setEmailVerifie(emailVerifie);
        uinfos.setNationalite(usagerNationalite);
        uinfos.setStatutFonctionPublique(usagerStatutFonctionPublique);
        uinfos.setUsagerCourrier(false);
        uinfos.setSub(usagerSub);
        if (type != null) {
            uinfos.setType(UsagerTypeEnum.valueOf(type.toUpperCase()));
        }

        constructInvoiceAdress(node, uinfos);
        uinfos.setTokenInfo(tokenInfo);

        // Récupération des informations MConnect si elles sont présentes
        JsonNode mconnect = node.get("mconnect-identity");
        if (mconnect != null && !(mconnect instanceof NullNode)) {
            try {

                StringNode givenNameNode0 = (StringNode) mconnect.get("given_name");
                StringNode familyNameNode = (StringNode) mconnect.get("family_name");
                StringNode mconnectBirthNameNode = (StringNode) mconnect.get("birth_name");
                StringNode genderNode = (StringNode) mconnect.get("gender");
                StringNode birthPlaceNode = (StringNode) mconnect.get("birth_place");
                StringNode birthDatetimeNode = (StringNode) mconnect.get("birth_datetime");
                StringNode authorityNode = (StringNode) mconnect.get("authority");
                StringNode birthPlaceCountryNode = (StringNode) mconnect.get("birth_place_country");
                StringNode birthPlaceCityNode = (StringNode) mconnect.get("birth_place_city");
                DonneesMConnectDTO mConnectUInfos = new DonneesMConnectDTO();
                mConnectUInfos.setGivenName(givenNameNode0.asString());
                mConnectUInfos.setFamilyName(familyNameNode.asString());
                mConnectUInfos.setBirthName(mconnectBirthNameNode.asString());
                mConnectUInfos.setGender(genderNode.asString());
                mConnectUInfos.setBirthPlace(birthPlaceNode.asString());
                mConnectUInfos.setBirthDatetime(
                        new SimpleDateFormat("yyyyMMddHHmmss").parse(birthDatetimeNode.asString()));
                mConnectUInfos.setAuthority(authorityNode.asString());
                mConnectUInfos.setBirthPlaceCountry(birthPlaceCountryNode.asString());
                mConnectUInfos.setBirthPlaceCity(birthPlaceCityNode.asString());
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode donneesExternes = mapper.createObjectNode();
                donneesExternes.set("mconnect", mapper.valueToTree(mConnectUInfos));
                uinfos.setDonneesExternes(donneesExternes);
                LOGGER.debug("Informations MConnect disponibles : {}", mConnectUInfos);
                uinfos.setMconnect(true);
                // Mettre login à "" si usager MConnect
                uinfos.setLogin("");
            } catch (ParseException e) {
                LOGGER.error("Erreur lors du parsing des informations certifiées MConnect", e);
            }
        }
        return uinfos;
    }

    private static void constructInvoiceAdress(ObjectNode node, UsagerInfosDTO uinfos) {
        JsonNode invoiceAddressNode = node.get("invoice_address");
        if (invoiceAddressNode != null && !invoiceAddressNode.isEmpty()) {
            AdresseFacturationDTO invoiceAddress = new AdresseFacturationDTO();
            String streetAddress =
                    invoiceAddressNode.get("street_address") != null ? invoiceAddressNode.get("street_address")
                                                                       .asString() : "";
            // On supprime les compléments d'adresse de l'adresse de facturation
            String[] split = streetAddress.split("\\n");
            invoiceAddress.setAdresse(split[0]);
            invoiceAddress.setComplAdresse1(split.length > 1 ? split[1] : "");
            invoiceAddress.setComplAdresse2(split.length == 3 ? split[2] : "");
            invoiceAddress.setCodePostal(invoiceAddressNode.path("postal_code").asString(""));
            invoiceAddress.setPaysCode(invoiceAddressNode.path("country").asString(""));
            invoiceAddress.setVille(invoiceAddressNode.path("locality").asString(""));
            uinfos.setAdresseFacturation(invoiceAddress);
        }
    }

    public HttpResponse logout(UsagerInfosDTO uinfos) {
        URL url = getURL("/protocol/openid-connect/logout");
        if (url == null) {
            LOGGER.error(NULL_URL);
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost(url.toString());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM, propertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                propertiesResolver.getGichkeyClientSecret()));
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

    public UsagerInfosDTO checkTokens(UsagerInfosDTO usagerInfosDTO, boolean forceRefresh) {

        LOGGER.debug("==================== KeycloakService.checkTokens() ...");
        UsagerInfosDTO ret;
        Date derniereObtention = usagerInfosDTO.getTokenInfo().getDateObtention();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(derniereObtention);
        // On prend la durée d'expiration moins 30 secondes pour avoir une marge
        calendar.add(Calendar.SECOND, usagerInfosDTO.getTokenInfo().getExpiresIn() - 30);
        Date expiration = calendar.getTime();
        Date now = new Date();
        LOGGER.debug("Dernière obtention = {}, expiration = {}, date courante = {}", derniereObtention, expiration,
                now);
        if (now.after(expiration) || forceRefresh) {
            LOGGER.debug("Il faut rafraîchir les tokens");

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
                ret = gichuniService.getGichuniApiProfileData(ret);
                ret.setAccessId(accessId);
            }
        } else {
            LOGGER.debug("Pas besoin de rafraîchir les tokens");
            ret = usagerInfosDTO;
        }

        LOGGER.debug("==================== FIN KeycloakService.checkTokens()");
        return ret;
    }

    public KeycloakTokenInfo refreshTokens(KeycloakTokenInfo tokenInfo) {
        URL url = getURL("/protocol/openid-connect/token");
        if (url == null) {
            LOGGER.error(NULL_URL);
            return null;
        }

        // Constitution de la requête
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost postRequest = new HttpPost(url.toString());

        LOGGER.debug("refreshToken utilisé pour appel : {}", tokenInfo.getRefreshToken());

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair(RequestConstant.REFRESH_TOKEN_PARAM, tokenInfo.getRefreshToken()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_ID_PARAM, propertiesResolver.getGichkeyClientId()));
        nvps.add(new BasicNameValuePair(RequestConstant.CLIENT_SECRET_PARAM,
                propertiesResolver.getGichkeyClientSecret()));
        nvps.add(new BasicNameValuePair("redirect_uri", propertiesResolver.getGichkeyKeycloakRedirectUrl()));
        nvps.add(new BasicNameValuePair("grant_type", RequestConstant.REFRESH_TOKEN_PARAM));
        nvps.add(new BasicNameValuePair(RequestConstant.SCOPE_PARAM, "openid mconnect monguichet"));

        postRequest.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        postRequest.setHeader(HttpHeaders.CONNECTION, KEEP_ALIVE);
        postRequest.setHeader(HttpHeaders.ACCEPT_ENCODING, ENCODING);
        postRequest.setHeader(HttpHeaders.ACCEPT, ACCEPT);

        LOGGER.info(APPEL_GICHKEY);
        try {
            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(postRequest);
            int statusCode = response.getCode();
            String resp = IOUtils.toString(response.getEntity().getContent());
            if (statusCode != 200) {
                LOGGER.error("Erreur lors de l'appel à GICHKEY : {}", resp);
                return null;
            }
            LOGGER.debug("Status code = {}, resp = {}", statusCode, resp);

            tokenInfo = jsonToTokenInfo(resp);
            tokenInfo.setDateObtention(new Date());

            LOGGER.debug("Nouvel accessToken={}", tokenInfo.getAccessToken());
            LOGGER.debug("Nouveau refreshToken={}", tokenInfo.getRefreshToken());

            LOGGER.debug("==================== FIN KeycloakService.checkTokens()");
            return tokenInfo;
        } catch (IOException e) {
            LOGGER.error(ERREUR_GICHKEY, e);
        }
        return null;
    }

}
