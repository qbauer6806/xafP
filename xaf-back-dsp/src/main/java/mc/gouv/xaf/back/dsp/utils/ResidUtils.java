package mc.gouv.xaf.back.dsp.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 
 * Classe utilitaire pour traiter les données RESID
 * 
 * @author amdiallo.ext
 *
 */
public class ResidUtils {

    public static final String DATE_RESID_FORMAT = "yyyy-MM-dd";
    public static final String DATE_YEAR_FORMAT = "yyyy";
    public static final String FULL_DATE_FORMAT = DATE_YEAR_FORMAT + "-MM-dd";
    public static final String DATE_TIME_FORMAT = FULL_DATE_FORMAT + "'T'HH:mm:ssXXX";
    public static final String FULL_HOUR_MIN = "HH:mm";

    private ResidUtils() {
        //DO NOTHING
    }

    /**
     * Calcul la date de naissance en fonction de la date donnée en entrée par
     * MConnect
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
     * Calcul l'heure de naissance en fonction de la date donnée en entrée par
     * MConnect
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
        LocalDateTime mConnectDateMinus1 = LocalDateTime
                .ofInstant(new SimpleDateFormat(DATE_TIME_FORMAT).parse(mConnectDateStr).toInstant(), ZoneId.systemDefault());
        Date mConnectDate = Date.from(mConnectDateMinus1.atZone(ZoneId.systemDefault()).toInstant());
        return new SimpleDateFormat(FULL_HOUR_MIN).format(mConnectDate);
    }
}
