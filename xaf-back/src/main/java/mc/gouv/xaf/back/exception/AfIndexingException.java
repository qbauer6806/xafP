package mc.gouv.xaf.back.exception;

/**
 * 
 * Exception declenchée si on a un problème d'indexation
 * @author asouabni.ext
 *
 */
public class AfIndexingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -5386824328579375094L;

    public AfIndexingException(String message, Throwable cause) {
        super(message, cause);
    }

    public AfIndexingException(String message) {
        super(message);
    }

}
