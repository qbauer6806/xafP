package mc.gouv.xaf.front.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocHolderFileUpdateDTO {

    private String filename;
    private String typedoc;
    private String preferredName;
    private String endOfValidity;

}
