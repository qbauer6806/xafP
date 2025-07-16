package mc.gouv.xaf.back.paiement.utils;

public class PaiementUtils {

    public static String calculateExpiration(String expiryMonth, String expiryYear) {
        StringBuilder result = new StringBuilder();
        int mois = Integer.parseInt(expiryMonth);
        result.append(String.format("%02d", mois)).append("/").append(expiryYear);
        return result.toString();
    }
}
