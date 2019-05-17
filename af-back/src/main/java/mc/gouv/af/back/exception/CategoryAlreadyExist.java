package mc.gouv.af.back.exception;

public class CategoryAlreadyExist extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -62796848163479781L;

    public CategoryAlreadyExist(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoryAlreadyExist(String message) {
        super(message);
    }
}
