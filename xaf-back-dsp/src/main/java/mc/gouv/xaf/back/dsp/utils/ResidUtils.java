package mc.gouv.xaf.back.dsp.utils;

import mc.gouv.xaf.back.dsp.dto.ResidAdresseDTO;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Classe utilitaire pour traiter les données RESID
 *
 * @author amdiallo.ext
 */
public class ResidUtils {

    public static final String DATE_RESID_FORMAT = "yyyy-MM-dd";
    public static final String DATE_YEAR_FORMAT = "yyyy";
    public static final String FULL_DATE_FORMAT = DATE_YEAR_FORMAT + "-MM-dd";
    public static final String DATE_TIME_FORMAT = FULL_DATE_FORMAT + "'T'HH:mm:ssXXX";
    public static final String FULL_HOUR_MIN = "HH:mm";
    private static final int MAX_LENGTH_ADRS_1 = 31;
    private static final int MAX_LENGTH_ADRS_2 = 37;
    private static final int MAX_LENGTH_ADRS_3 = 38;
    private static final int MAX_LENGTH_ADRS_4 = 37;

    private ResidUtils() {
        //DO NOTHING
    }

    /**
     * Calcul la date de naissance en fonction de la date donnée en entrée par MConnect
     *
     * @param mConnectDate format MConnect (1991-12-26T13:30:00+01:00)
     * @return : la date de naissance au format compliant avec l'API de resid (YYYY-MM-dd)
     * @throws ParseException
     */
    public static String convertMConnectDateToResidDate(final String mConnectDate) throws ParseException {
        if (StringUtils.isBlank(mConnectDate)) {
            return StringUtils.EMPTY;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_RESID_FORMAT);
        Date parse = new SimpleDateFormat(DATE_TIME_FORMAT).parse(mConnectDate);
        return simpleDateFormat.format(parse);
    }

    /**
     * Calcul l'heure de naissance en fonction de la date donnée en entrée par MConnect
     *
     * @param mConnectDateStr format MConnect (1991-12-26T13:30:00+01:00)
     * @return : l'heure de naissance au format compliant avec l'API de resid (HH:mm)
     * @throws ParseException
     */
    public static String convertMConnectDateToResidHourMinute(final String mConnectDateStr) throws ParseException {
        if (StringUtils.isBlank(mConnectDateStr)) {
            return StringUtils.EMPTY;
        }
        // Date MConnect donnée en +1h, mise à l'heure 0 à faire avant conversion
        LocalDateTime mConnectDateMinus1 = LocalDateTime.ofInstant(
                new SimpleDateFormat(DATE_TIME_FORMAT).parse(mConnectDateStr).toInstant(), ZoneId.systemDefault());
        Date mConnectDate = Date.from(mConnectDateMinus1.atZone(ZoneId.systemDefault()).toInstant());
        return new SimpleDateFormat(FULL_HOUR_MIN).format(mConnectDate);
    }

    /**
     * Permets d'initialiser l'adresse RESID à partir de ligne 1, 2 et 3 de l'adresse du TS
     *
     * @param ligne1
     * @param ligne2
     * @param ligne3
     * @return
     */
    public static ResidAdresseDTO getResidAdresseDTO(String ligne1, String ligne2, String ligne3) {
        ResidAdresseDTO adresseDTO = new ResidAdresseDTO();
        //Par défaut, adresse 1 correspond à la ligne 2 et adresse 2 correspond à la ligne 1
        //Mais dans RESID, c'est l'adresse 2 qui est obligatoire. Donc, on valorise ce champ en fonction des données saisies dans RESPRIM
        if (StringUtils.isNotBlank(ligne1)) {
            adresseDTO.setAdresse1(truncateAdresse(ligne2, MAX_LENGTH_ADRS_1));
            adresseDTO.setAdresse2(truncateAdresse(ligne1, MAX_LENGTH_ADRS_2));
            adresseDTO.setAdresse3(truncateAdresse(ligne3, MAX_LENGTH_ADRS_3));
        } else if (StringUtils.isNotBlank(ligne2)) {
            adresseDTO.setAdresse2(truncateAdresse(ligne2, MAX_LENGTH_ADRS_2));
            adresseDTO.setAdresse3(truncateAdresse(ligne3, MAX_LENGTH_ADRS_3));
        } else {
            adresseDTO.setAdresse2(truncateAdresse(ligne3, MAX_LENGTH_ADRS_2));
        }

        return adresseDTO;
    }

    /**
     * Permets de construire adresse 4 RESID à partir du code post et ville
     *
     * @param codePostal
     * @param ville
     * @return
     */
    public static String getAdresse4(String codePostal, String ville) {
        String adrs4 = "";
        if (StringUtils.isNotBlank(codePostal) && StringUtils.isNotBlank(ville)) {
            adrs4 = codePostal + ", " + ville;
        } else if (StringUtils.isNotBlank(codePostal)) {
            adrs4 = codePostal;
        } else if (StringUtils.isNotBlank(ville)) {
            adrs4 = ville;
        }
        return truncateAdresse(adrs4, MAX_LENGTH_ADRS_4);
    }

    private static String truncateAdresse(String adresse, final int taille) {
        return StringUtils.isNotEmpty(adresse) && adresse.length() > taille ? adresse.substring(0, taille) : adresse;
    }
}
