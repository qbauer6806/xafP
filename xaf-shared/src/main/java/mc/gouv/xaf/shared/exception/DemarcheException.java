package mc.gouv.xaf.shared.exception;

import java.io.Serial;

public class DemarcheException extends RuntimeException {

    /**
	 * 
	 */
	@Serial
    private static final long serialVersionUID = 5931833189971344224L;

	public DemarcheException(String message) {
        super(message);
    }

    public DemarcheException(String message, Throwable cause) {
        super(message, cause);
    }

    public DemarcheException(Throwable cause) {
        super(cause);
    }
    
}
