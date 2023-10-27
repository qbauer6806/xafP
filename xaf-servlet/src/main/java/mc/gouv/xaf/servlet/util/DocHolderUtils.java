package mc.gouv.xaf.servlet.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static mc.gouv.xaf.servlet.util.AppFactoryServletUtils.getAfApiClient;

public class DocHolderUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderUtils.class);
    public static final String DOCHOLDER_CONSENT_NODE = "docholderConsent";
    public static final String CONSENTING_NODE = "consenting";
    public static final String DATE_CREATION_NODE = "dateCreation";
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private DocHolderUtils() {

    }

    /**
     * Met à jour la date de consentement du porte-documents à la date de l'instant.
     *
     * @param usagerId l'identifiant usager
     * @return true si le consentement TS est valide et que la date pu être mise à jour. false sinon
     */
    public static boolean updateConsentDate(Integer usagerId) {
        AccessDTO access = getAfApiClient().getAccess(usagerId);
        if (access != null && access.getContenu() != null) {
            boolean consent = access.getContenu().findPath(DOCHOLDER_CONSENT_NODE).findPath(CONSENTING_NODE).asBoolean();
            if (consent) {
                AccessInputDTO accessInputDTO = new AccessInputDTO();
                ((ObjectNode) access.getContenu().findPath(DOCHOLDER_CONSENT_NODE)).put(DATE_CREATION_NODE, new SimpleDateFormat(JSON_DATE_FORMAT).format(Date.from(LocalDateTime.now().atZone(ZoneId.of("Europe/Monaco")).toInstant())));
                accessInputDTO.setContenu(access.getContenu());

                getAfApiClient().createOrUpdateAccess(usagerId, accessInputDTO);

                return true;
            }
        }

        return false;
    }

    /**
     * Permet de savoir si l'usager a consenti à l'utilisation du porte-documents côté TS
     *
     * @param usagerId l'identifiant du compe usager
     * @return true si l'usager a consenti côté TS ET que sa date de consentement n'est pas arrivée à expiration
     */
    public static boolean isConsenting(Integer usagerId) {
        AccessDTO access = getAfApiClient().getAccess(usagerId);

        if (access == null || access.getContenu() == null) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(JSON_DATE_FORMAT);
        JsonNode dateNode = access.getContenu().findPath(DOCHOLDER_CONSENT_NODE).findPath(DATE_CREATION_NODE);

        if (StringUtils.isEmpty(dateNode.textValue())) {
            return false;
        }

        try {
            boolean consent = access.getContenu().findPath(DOCHOLDER_CONSENT_NODE).findPath(CONSENTING_NODE).asBoolean();
            Date dateConsent = dateFormat.parse(dateNode.asText());
            Date oneYearPlusOneMonth = Date.from(dateConsent.toInstant().atZone(ZoneId.of("Europe/Monaco")).plusYears(1).plusMonths(1).toInstant());
            Date today = Date.from(Instant.now().atZone(ZoneId.of("Europe/Monaco")).toInstant());

            return consent && (oneYearPlusOneMonth.before(today));

        } catch (ParseException e) {
            LOGGER.error("Impossible de parser la date de consentement au porte-documents de l'utilisateur", e);
            return false;
        }
    }
}
