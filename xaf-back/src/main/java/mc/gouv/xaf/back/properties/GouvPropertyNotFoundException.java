package mc.gouv.xaf.back.properties;

import java.util.List;

public class GouvPropertyNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 5998373316409301775L;

    public GouvPropertyNotFoundException(String prop) {
        super(prop + " inexistant dans les fichiers de properties");
    }

    public GouvPropertyNotFoundException(List<String> props) {
        super(computeMessage(props));
    }

    private static String computeMessage(List<String> props) {
        StringBuffer strBuff = new StringBuffer();
        for (String prop : props) {
            strBuff.append(prop);
            strBuff.append(", ");
        }
        return strBuff + " inexistants dans les fichiers de properties";
    }

}
