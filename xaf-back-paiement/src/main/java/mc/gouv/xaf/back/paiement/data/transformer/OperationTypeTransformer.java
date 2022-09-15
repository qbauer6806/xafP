package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class OperationTypeTransformer {

    private OperationTypeTransformer() {
    }

    public static String bo2String(OperationTypeEnum bo) {
        return bo.name();
    }

    public static OperationTypeEnum string2Bo(String str) {
        return OperationTypeEnum.valueOf(str);
    }

    public static List<String> bos2Strings(List<OperationTypeEnum> bos) {
        ArrayList<String> strs = new ArrayList<>();
        for (OperationTypeEnum bo : bos) {
            strs.add(bo2String(bo));
        }
        return strs;
    }

    public static List<OperationTypeEnum> strings2Bos(List<String> strs) {
        ArrayList<OperationTypeEnum> bos = new ArrayList<>();
        for (String dto : strs) {
            bos.add(string2Bo(dto));
        }
        return bos;
    }

}
