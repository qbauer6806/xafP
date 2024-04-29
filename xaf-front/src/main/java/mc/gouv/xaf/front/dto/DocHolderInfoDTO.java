package mc.gouv.xaf.front.dto;

public class DocHolderInfoDTO {
    private boolean consenting;
    private boolean docHolderCreated;
    private long documentCount;

    public boolean isConsenting() {
        return consenting;
    }

    public void setConsenting(boolean consenting) {
        this.consenting = consenting;
    }

    public boolean isDocHolderCreated() {
        return docHolderCreated;
    }

    public void setDocHolderCreated(boolean docHolderCreated) {
        this.docHolderCreated = docHolderCreated;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }
}
