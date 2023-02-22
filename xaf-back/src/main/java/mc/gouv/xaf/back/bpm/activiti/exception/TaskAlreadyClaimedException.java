package mc.gouv.xaf.back.bpm.activiti.exception;

public class TaskAlreadyClaimedException extends Exception {

    private static final long serialVersionUID = -5746531461835816304L;

    public TaskAlreadyClaimedException(Exception e) {
        super(e);
    }

    public TaskAlreadyClaimedException(String message, Exception e) {
        super(message, e);
    }
    
    public TaskAlreadyClaimedException(String message) {
        super(message);
    }

}
