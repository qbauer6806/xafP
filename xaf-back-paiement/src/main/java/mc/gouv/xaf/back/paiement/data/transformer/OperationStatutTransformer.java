package mc.gouv.xaf.back.paiement.data.transformer;

import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mpavone.ext
 */
public class OperationStatutTransformer {

    private OperationStatutTransformer() {
    }

    public static String bo2String(OperationStatutEnum bo) {
        return bo != null ? bo.name() : null;
    }

    public static OperationStatutEnum string2Bo(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return OperationStatutEnum.valueOf(str);
    }

    public static List<String> bos2Strings(List<OperationStatutEnum> bos) {
        ArrayList<String> strs = new ArrayList<>();
        for (OperationStatutEnum bo : bos) {
            strs.add(bo2String(bo));
        }
        return strs;
    }

    public static List<OperationStatutEnum> strings2Bos(List<String> strs) {
        ArrayList<OperationStatutEnum> bos = new ArrayList<>();
        for (String dto : strs) {
            bos.add(string2Bo(dto));
        }
        return bos;
    }

}
