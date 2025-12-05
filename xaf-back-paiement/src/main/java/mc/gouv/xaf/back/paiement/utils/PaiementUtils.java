package mc.gouv.xaf.back.paiement.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class PaiementUtils {

    public static String calculateExpiration(Integer expiryMonth, Integer expiryYear) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%02d", expiryMonth)).append("/").append(expiryYear);
        return result.toString();
    }

    public static LocalDateTime toUtc(LocalDateTime local) {
        if (local == null) {
            return null;
        }
        ZonedDateTime systemZdt = local.atZone(ZoneId.systemDefault());
        return systemZdt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private PaiementUtils() {
        throw new IllegalStateException("Utility class");
    }
}
