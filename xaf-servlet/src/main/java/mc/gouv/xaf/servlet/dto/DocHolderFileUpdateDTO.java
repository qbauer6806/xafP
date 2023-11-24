package mc.gouv.xaf.servlet.dto;

public class DocHolderFileUpdateDTO {
    private String filename;
    private String typedoc;
    private String preferredName;
    private String endOfValidity;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTypedoc() {
        return typedoc;
    }

    public void setTypedoc(String typedoc) {
        this.typedoc = typedoc;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getEndOfValidity() {
        return endOfValidity;
    }

    public void setEndOfValidity(String endOfValidity) {
        this.endOfValidity = endOfValidity;
    }
}
