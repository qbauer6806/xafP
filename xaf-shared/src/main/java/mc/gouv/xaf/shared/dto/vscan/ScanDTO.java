package mc.gouv.xaf.shared.dto.vscan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class ScanDTO {
    private UUID identifiant;
    private String codeAppli;
    private String fileName;
    private String enduserAppModule;
    private String jwtSub;
    private Set<MetadataDTO> metadata;
    private String fileSha256;
    private Long fileSize;
    @JsonFormat(
            locale = "fr",
            shape = Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            timezone = "GMT+1"
    )
    private Date date;
    private boolean result;
}
