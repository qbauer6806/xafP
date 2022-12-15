package mc.gouv.xaf.rio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RioDocumentRequestDTO {

    private String codeApplication;
    private String lastModifier;
    private String codeNotice;

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
}
