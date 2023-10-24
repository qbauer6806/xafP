package mc.gouv.xaf.servlet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class DocHolderConsentDTO {
    private boolean consenting;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss:SSS")
    private Date dateCreation;

    public boolean isConsenting() {
        return consenting;
    }

    public void setConsenting(boolean consenting) {
        this.consenting = consenting;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
}
