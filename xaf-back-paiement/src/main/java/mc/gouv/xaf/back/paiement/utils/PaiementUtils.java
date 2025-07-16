package mc.gouv.xaf.back.paiement.utils;

public class PaiementUtils {

    public static String calculateExpiration(Integer expiryMonth, Integer expiryYear) {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%02d", expiryMonth)).append("/").append(expiryYear);
        return result.toString();
    }
}
