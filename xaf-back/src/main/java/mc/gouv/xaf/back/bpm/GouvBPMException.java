package mc.gouv.xaf.back.bpm;

/**
 * Modélise une exception du BPM du gouvernement
 *
 * @author qdeme
 */
public class GouvBPMException extends RuntimeException {

    private static final long serialVersionUID = -8024816609555494712L;

    public GouvBPMException(String message, Throwable cause) {
        super(message, cause);
    }

    public GouvBPMException(String message) {
        super(message);
    }

}
