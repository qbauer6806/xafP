package mc.gouv.xaf.back.exception;

import mc.gouv.xaf.back.exception.enums.FileUploadErrorEnum;

public class FileUploadException extends RuntimeException {

    private static final long serialVersionUID = 8773465036895573949L;

    private FileUploadErrorEnum error;

    public FileUploadException(String message, FileUploadErrorEnum error) {
        super(message);
        this.error = error;
    }

    public FileUploadErrorEnum getError() {
        return error;
    }

    public void setError(FileUploadErrorEnum error) {
        this.error = error;
    }
}
