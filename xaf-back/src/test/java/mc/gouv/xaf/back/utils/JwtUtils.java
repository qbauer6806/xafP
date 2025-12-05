package mc.gouv.xaf.back.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    public static void main(String[] args) {
        // STAGE_FRONT_JWT
        String jwt = createJWTToken("secretsecretsecretsecretsecretsecret", "ENRBAIL", new String[] { "USER" }, "ENRBAIL", null,
                null);

        //        // STAGE_FRONT_FILE_JWT
        //        String jwt = createJWTToken("secretsecretsecretsecretsecretsecret", "STAGE", new String[] { "USER" }, "FILE", null, null);
        //
        //        // STAGE_FRONT_VSCAN_JWT
        //        String jwt = createJWTToken("secretsecretsecretsecretsecretsecret", "STAGE", new String[] { "USER" }, "VSCAN", null, null);

        LOGGER.info(jwt);
    }

    public static String createJWTToken(String secret, String sub, String[] roles, String aud, String applicationName,
            Object appClaims) {
        var jwtbuilder = Jwts.builder().setSubject(sub)
                .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
                .setHeaderParam("typ", "JWT").claim("aud", aud).claim("iat", new Date())
                .claim("jti", UUID.randomUUID().toString());

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

}

