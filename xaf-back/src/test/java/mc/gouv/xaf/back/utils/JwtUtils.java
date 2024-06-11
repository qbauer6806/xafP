package mc.gouv.xaf.back.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mc.gouv.xaf.back.config.utils.XafSpringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    public static void main(String[] args) {

        String jwt = createJWTToken("secret", "INSENPR", new String[] { "USER" }, "FILE", null, null);
        LOGGER.info(jwt);
    }

    public static String createJWTToken(String secret, String sub, String[] roles, String aud, String applicationName,
            Object appClaims) {
        var iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        var jwtbuilder = Jwts.builder().setSubject(sub)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8)).setHeaderParam("typ", "JWT")
                .claim("aud", aud).claim("iat", iso8601DateFormat.format(new Date())).claim("jti", UUID.randomUUID());

        HashMap<String, Object> gouvMap = new HashMap<>();
        HashMap<String, Object> rolesMap = new HashMap<>();

        rolesMap.put("roles", roles);
        gouvMap.put("shared", rolesMap);

        if (StringUtils.isNotEmpty(applicationName)) {
            //Ajout des claims spécifique pour une application donnée
            gouvMap.put(applicationName, appClaims);
        }

        jwtbuilder.claim("gouv", gouvMap);

        return jwtbuilder.compact();
    }

    public static HashMap<String, Object> getParams(String key, String... values) {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put(key, values);
        return paramsMap;
    }

    public static String createJwtHeaderValue(String secret, String sub, String[] roles, String aud) {
        return XafSpringUtils.JWT_PREFIX + createJWTToken(secret, sub, roles, aud, null, null);
    }

    public static String createJwtHeaderValue(String secret, String sub, String[] roles, String aud,
            String applicationName, Object appClaims) {
        return XafSpringUtils.JWT_PREFIX + createJWTToken(secret, sub, roles, aud, applicationName, appClaims);
    }

    public static String createJwtHeaderValue(String secret, String sub, String role, String aud) {
        return XafSpringUtils.JWT_PREFIX + createJWTToken(secret, sub, new String[] { role }, aud, null, null);
    }

}

class GouvClaims {

    private Map<String, Object> shared = new HashMap<>();

    GouvClaims(String[] roles) {
        shared.put("roles", roles);
    }

    public Map<String, Object> getShared() {
        return shared;
    }

    public void setShared(Map<String, Object> shared) {
        this.shared = shared;
    }

}
