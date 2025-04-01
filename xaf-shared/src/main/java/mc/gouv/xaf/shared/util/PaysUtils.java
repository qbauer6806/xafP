package mc.gouv.xaf.shared.util;

import mc.gouv.xaf.shared.dto.PaysDTO;

/**
 * Classe utilitaire pour initialiser une valeur non connue dans les listes des pays et nationalités
 */
public class PaysUtils {

    private static final String LIBELLE_NON_CONNU_FR = "Non connu";
    private static final String LIBELLE_NON_CONNU_EN = "Unknown";

    private PaysUtils() {
        //DO NOTHING
    }

    public static PaysDTO initValeurNonConnue() {
        PaysDTO paysDTO = new PaysDTO();
        paysDTO.setCode("ZZ");
        paysDTO.setCodeAlpha3("XXX");
        paysDTO.setLibelle(LIBELLE_NON_CONNU_FR);
        paysDTO.setLibelleEn(LIBELLE_NON_CONNU_EN);
        paysDTO.setLibelleLong(LIBELLE_NON_CONNU_FR);
        paysDTO.setLibelleLongEn(LIBELLE_NON_CONNU_EN);
        paysDTO.setOrdre(9999);
        paysDTO.setNationalite(LIBELLE_NON_CONNU_FR);
        paysDTO.setNationaliteEn(LIBELLE_NON_CONNU_EN);
        paysDTO.setNationaliteCode("ZZ");

        return paysDTO;
    }
}
