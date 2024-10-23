package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidMembresFoyerDLN1FDTO implements Serializable {

    private static final long serialVersionUID = 5592048407903642368L;

    private boolean hasOtherPersonne;

    private Integer nombreOtherPersonne;

    private List<ResidPersonneDLN1FDTO> personne;

}
