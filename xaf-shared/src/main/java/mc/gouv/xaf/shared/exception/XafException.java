package mc.gouv.xaf.shared.exception;

import java.io.Serial;

public class XafException extends RuntimeException {

    @Serial
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
