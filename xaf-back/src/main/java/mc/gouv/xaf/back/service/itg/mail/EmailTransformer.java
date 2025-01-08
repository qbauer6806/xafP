package mc.gouv.xaf.back.service.itg.mail;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.shared.dto.mail.AddressBlockDTO;
import mc.gouv.xaf.shared.dto.mail.ParamDTO;

/**
 * 
 * Transformer pour les emails
 * 
 * @author qdeme
 */
public class EmailTransformer {

    private EmailTransformer() {
    }

    public static AddressBlockDTO toMailApiAddress(EmailInfoAddressDTO addr) {
        if (addr == null) {
            return null;
        }
        AddressBlockDTO to = new AddressBlockDTO();
        to.setAddress(addr.getAddress());
        to.setName(addr.getName());
        return to;
    }

    public static List<AddressBlockDTO> toMailApiAddresses(List<EmailInfoAddressDTO> addrList) {
        List<AddressBlockDTO> to = new ArrayList<>();
        for (EmailInfoAddressDTO addr : addrList) {
            to.add(toMailApiAddress(addr));
        }
        return to;
    }

    public static ParamDTO toMailApiParam(EmailInfoParamDTO param) {
        if (param == null) {
            return null;
        }
        ParamDTO retParam = new ParamDTO();
        retParam.setKey(param.getKey());
        retParam.setValue(param.getValue());
        return retParam;
    }

    public static List<ParamDTO> toMailApiParams(List<EmailInfoParamDTO> params) {
        List<ParamDTO> retParams = new ArrayList<>();
        for (EmailInfoParamDTO param : params) {
            retParams.add(toMailApiParam(param));
        }
        return retParams;
    }

}
