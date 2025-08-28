package mc.gouv.xaf.back.service.itg.sms;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.back.service.itg.sms.dto.SmsInfoParamDTO;
import mc.gouv.xaf.back.service.itg.sms.dto.SmsParamDTO;

/**
 * 
 * Transformer pour les SMS
 * 
 * @author qdeme
 */
public class SmsTransformer {

    private SmsTransformer() {
    }

    public static SmsParamDTO toSmsApiParam(SmsInfoParamDTO param) {
        if (param == null) {
            return null;
        }
        SmsParamDTO retParam = new SmsParamDTO();
        retParam.setKey(param.getKey());
        retParam.setValue(param.getValue());
        return retParam;
    }

    public static List<SmsParamDTO> toSmsApiParams(List<SmsInfoParamDTO> params) {
        List<SmsParamDTO> retParams = new ArrayList<>();
        for (SmsInfoParamDTO param : params) {
            retParams.add(toSmsApiParam(param));
        }
        return retParams;
    }

}
