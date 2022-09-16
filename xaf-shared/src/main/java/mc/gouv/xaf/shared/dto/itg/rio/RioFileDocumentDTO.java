package mc.gouv.xaf.shared.dto.itg.rio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RioFileDocumentDTO {

    private Long keyFile;
    private String filename;
    private Integer rank;
    private Long fileSize;
    private String description;

    public Long getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(Long keyFile) {
        this.keyFile = keyFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
