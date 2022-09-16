package mc.gouv.xaf.shared.dto.itg.rio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RioDocumentDTO {

    private String codeApplication;
    private String lastModifier;
    private String codeNotice;
    private String refDocument;
    private Long keyDocument;
    private String creator;
    private String creationDate;
    private String modificationDate;
    private String eraser;
    private String removalDate;
    private List<RioFileDocumentDTO> attachments;

    public String getCodeApplication() {
        return codeApplication;
    }

    public void setCodeApplication(String codeApplication) {
        this.codeApplication = codeApplication;
    }

    public String getLastModifier() {
        return lastModifier;
    }

    public void setLastModifier(String lastModifier) {
        this.lastModifier = lastModifier;
    }

    public String getCodeNotice() {
        return codeNotice;
    }

    public void setCodeNotice(String codeNotice) {
        this.codeNotice = codeNotice;
    }

    public String getRefDocument() {
        return refDocument;
    }

    public void setRefDocument(String refDocument) {
        this.refDocument = refDocument;
    }

    public Long getKeyDocument() {
        return keyDocument;
    }

    public void setKeyDocument(Long keyDocument) {
        this.keyDocument = keyDocument;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getEraser() {
        return eraser;
    }

    public void setEraser(String eraser) {
        this.eraser = eraser;
    }

    public String getRemovalDate() {
        return removalDate;
    }

    public void setRemovalDate(String removalDate) {
        this.removalDate = removalDate;
    }

    public List<RioFileDocumentDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<RioFileDocumentDTO> attachments) {
        this.attachments = attachments;
    }
}
