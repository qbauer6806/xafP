package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DonneesMConnectDTO {

    private String givenName;

    private String familyName;

    private String birthName;

    private String gender;

    private String birthPlace;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmmss", timezone = "GMT+1")
    private Date birthDatetime;

    private String authority;

    private String birthPlaceCountry;

    private String birthPlaceCity;

}
