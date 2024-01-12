package mc.gouv.candifp.frontserver.movetoxaf.properties;

import java.util.List;

public class GouvPropertyNotFoundException extends Exception {

    private static final long serialVersionUID = 5998373316409301775L;

    public GouvPropertyNotFoundException(String prop) {
        super(prop + " inexistant dans les fichiers de properties");
    }

    public GouvPropertyNotFoundException(List<String> props) {
        super(computeMessage(props));
    }

    private static String computeMessage(List<String> props) {
        StringBuilder builder = new StringBuilder();
        for (String prop : props) {
            builder.append(prop);
            builder.append(", ");
        }
        builder.append(" inexistants dans les fichiers de properties");
        return builder.toString();
    }

}
