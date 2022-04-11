package mc.gouv.xaf.back.exception;

public class XafException extends RuntimeException {

    private static final long serialVersionUID = -653180311184252706L;

    public XafException(String message) {
        super(message);
    }

    public XafException(String message, Throwable cause) {
        super(message, cause);
    }

    public XafException(Throwable cause) {
        super(cause);
    }
}
