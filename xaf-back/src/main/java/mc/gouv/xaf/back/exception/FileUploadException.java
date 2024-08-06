package mc.gouv.xaf.back.exception;

import java.io.Serial;
import lombok.Getter;
import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;

@Getter
public class FileUploadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8773465036895573949L;

    private final FileUploadErrorEnum error;

    public FileUploadException(String message, FileUploadErrorEnum error) {
        super(message);
        this.error = error;
    }

}
