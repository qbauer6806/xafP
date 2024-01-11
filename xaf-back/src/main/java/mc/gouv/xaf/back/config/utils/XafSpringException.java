package mc.gouv.xaf.back.config.utils;

/**
 * Classe d'exceptions spécifiques à la démarche
 *
 * @author mpavone
 */
public class XafSpringException extends RuntimeException {

    private static final long serialVersionUID = 4068949092531091946L;

    public XafSpringException(String message) {
        super(message);
    }

    public XafSpringException(Throwable throwable) {
        super(throwable);
    }

    public XafSpringException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
