package mc.gouv.xaf.servlet.dto;

public class DocHolderFilePostDTO {
    private String url;
    private String typedoc;
    private String preferredName;
    private String endOfValidity;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
