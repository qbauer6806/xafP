package mc.gouv.af.back.mail;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.mail.shared.dto.AddressBlockDTO;
import mc.gouv.mail.shared.dto.ParamDTO;

public class EmailTransformer {

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
        List<AddressBlockDTO> to = new ArrayList<AddressBlockDTO>();
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
        List<ParamDTO> retParams = new ArrayList<ParamDTO>();
        for (EmailInfoParamDTO param : params) {
            retParams.add(toMailApiParam(param));
        }
        return retParams;
    }
    
}
