package mc.gouv.xaf.back.dem;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour les tests
 * 
 * @author qdeme
 *
 */
@Component
public class TestUtils {

    public static final String FRONT_USER_PWD = "frontpass";
    public static final String FRONT_USER_NAME = "frontuser";
    public static final String BACK_USER_PWD = "backpass";
    public static final String BACK_USER_NAME = "backuser";
    protected static final String FRONT_USER_ROLE = "FRONT";
    protected static final String BACK_USER_ROLE = "BACK";
    protected static final String JWT_SECRET = "supersecret";
//    public static String CONTAINERID = null;
//    public static String FILE_REST_URL = null;
    public static String FILE_JWT = null;
    public static String USAGERS_REST_URL = null;

//    @Autowired
//    private DemGouvPropertiesResolver demGouvPropertiesResolver;
//
//    @PostConstruct
//    public void fillConstants() {
//        CONTAINERID = demGouvPropertiesResolver.getValue(DemGouvProperty.CONTAINERID);
//        FILE_REST_URL = demGouvPropertiesResolver.getValue(DemGouvProperty.FILE_REST_URL);
//    }

    public static String encryptLowercasedPassword(String pwd) {
        return DigestUtils.sha1Hex(pwd.toLowerCase().getBytes());
    }

}
