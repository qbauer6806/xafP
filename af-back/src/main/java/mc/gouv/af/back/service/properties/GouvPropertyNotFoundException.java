package mc.gouv.af.back.service.properties;

import java.util.List;

public class GouvPropertyNotFoundException extends Exception {

    public GouvPropertyNotFoundException(GouvProperty prop) {
        super(prop.getCode() + " inexistant dans les fichiers de properties");
    }

    public GouvPropertyNotFoundException(List<GouvProperty> props) {
        super(computeMessage(props));
    }

    private static String computeMessage(List<GouvProperty> props) {
        StringBuffer strBuff = new StringBuffer();
        for (GouvProperty prop : props) {
            strBuff.append(prop.getCode());
            strBuff.append(", ");
        }
        return strBuff + " inexistants dans les fichiers de properties";
    }

}
