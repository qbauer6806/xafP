package mc.gouv.af.back.exception;

public class UsedCategoryException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 8228971845578127658L;

    public UsedCategoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsedCategoryException(String message) {
        super(message);
    }
}
