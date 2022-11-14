package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class MoyenPaiementStatutTransformer {

    private MoyenPaiementStatutTransformer() {
    }

    public static String bo2String(MoyenPaiementStatutEnum bo) {
        return bo != null ? bo.name() : null;
    }

    public static MoyenPaiementStatutEnum string2Bo(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return MoyenPaiementStatutEnum.valueOf(str);
    }

    public static List<String> bos2Strings(List<MoyenPaiementStatutEnum> bos) {
        ArrayList<String> strs = new ArrayList<>();
        for (MoyenPaiementStatutEnum bo : bos) {
            strs.add(bo2String(bo));
        }
        return strs;
    }

    public static List<MoyenPaiementStatutEnum> strings2Bos(List<String> strs) {
        ArrayList<MoyenPaiementStatutEnum> bos = new ArrayList<>();
        for (String dto : strs) {
            bos.add(string2Bo(dto));
        }
        return bos;
    }

}
