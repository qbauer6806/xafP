package mc.gouv.xaf.back.properties;

public class DemPropertyNotFoundException extends Exception {

    private static final long serialVersionUID = 2405372166053138405L;

    public DemPropertyNotFoundException(String propertyName) {
        super("La DEM_PROPERTIES \"" + propertyName + "\" n'a pas pu être trouvée en base !");
    }

}
