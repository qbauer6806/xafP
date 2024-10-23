package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidContactDNL1FDTO implements Serializable {

    private static final long serialVersionUID = 7508929356926248233L;

    private String email;

    private String telephone1Prefix;

    private String telephone1;

    private String typeCommunication;

    private String langue;

}
