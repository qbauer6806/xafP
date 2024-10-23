package mc.gouv.xaf.front.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.front.util.DocHolderUtils;

import java.util.Date;

@Setter
@Getter
public class DocHolderConsentDTO {

    private boolean consenting;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DocHolderUtils.JSON_DATE_FORMAT, timezone = "Europe/Monaco")
    private Date dateCreation;

}
