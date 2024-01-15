package mc.gouv.xaf.front.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import mc.gouv.xaf.front.util.DocHolderUtils;

import java.util.Date;

public class DocHolderConsentDTO {
    private boolean consenting;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DocHolderUtils.JSON_DATE_FORMAT, timezone = "Europe/Monaco")
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
