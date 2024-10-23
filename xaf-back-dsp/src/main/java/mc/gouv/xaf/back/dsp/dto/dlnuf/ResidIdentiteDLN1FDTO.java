package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidIdentiteDLN1FDTO implements Serializable {

    private static final long serialVersionUID = 2762334113370075251L;

    private ResidCiviliteEnum titreUsager;

    private String nomUsager;

    private String nomUsageUsager;

    private String prenomUsager;

    private String dateNaissanceUsager;

    private String heureNaissanceUsager;

    private String villeNaissanceUsager;

    private String paysNaissanceUsager;

    private ResidSexeEnum sexeUsager;

    private boolean personnaliteSensible;

}
