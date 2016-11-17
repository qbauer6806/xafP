package mc.gouv.af.back.mail;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.mail.apishared.model.AddressBlock;
import mc.gouv.mail.apishared.model.Param;

public class EmailTransform {

    public static AddressBlock toMailApiAddress(EmailInfoAddressDTO addr) {
        if (addr == null) {
            return null;
        }
        AddressBlock to = new AddressBlock();
        to.setAddress(addr.getAddress());
        to.setName(addr.getName());
        return to;
    }
    
    public static List<AddressBlock> toMailApiAddresses(List<EmailInfoAddressDTO> addrList) {
        List<AddressBlock> to = new ArrayList<AddressBlock>();
        for (EmailInfoAddressDTO addr : addrList) {
            to.add(toMailApiAddress(addr));
        }
        return to;
    }
    
    public static Param toMailApiParam(EmailInfoParamDTO param) {
        if (param == null) {
            return null;
        }
        Param retParam = new Param();
        retParam.setKey(param.getKey());
        retParam.setValue(param.getValue());
        return retParam;
    }

    public static List<Param> toMailApiParams(List<EmailInfoParamDTO> params) {
        List<Param> retParams = new ArrayList<Param>();
        for (EmailInfoParamDTO param : params) {
            retParams.add(toMailApiParam(param));
        }
        return retParams;
    }
    
}
