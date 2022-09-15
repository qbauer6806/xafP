package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class MoyenPaiementTypeTransformer {

    private MoyenPaiementTypeTransformer() {
    }

    public static String bo2String(MoyenPaiementTypeEnum bo) {
        return bo != null ? bo.name() : null;
    }

    public static MoyenPaiementTypeEnum string2Bo(String str) {
        return MoyenPaiementTypeEnum.valueOf(str);
    }

    public static List<String> bos2Strings(List<MoyenPaiementTypeEnum> bos) {
        ArrayList<String> strs = new ArrayList<>();
        for (MoyenPaiementTypeEnum bo : bos) {
            strs.add(bo2String(bo));
        }
        return strs;
    }

    public static List<MoyenPaiementTypeEnum> strings2Bos(List<String> strs) {
        ArrayList<MoyenPaiementTypeEnum> bos = new ArrayList<>();
        for (String dto : strs) {
            bos.add(string2Bo(dto));
        }
        return bos;
    }

}
