package mc.gouv.xaf.back.exception;

public class DemarcheException extends RuntimeException {

    /**
	 * 
	 */
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
