package mc.gouv.appfactory.dto;

public class FileUploadResponseDTO {
    
    private String fileId;
    
    public FileUploadResponseDTO(String fileId) {
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

}
