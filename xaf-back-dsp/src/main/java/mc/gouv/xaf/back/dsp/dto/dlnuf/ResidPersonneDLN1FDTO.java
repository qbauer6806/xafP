package mc.gouv.xaf.back.dsp.dto.dlnuf;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationDLN1FEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidPersonneDLN1FDTO implements Serializable {

	private static final long serialVersionUID = 2524864271822373336L;
	
	private ResidRelationDLN1FEnum relationPersonne;
	
	private ResidCiviliteEnum titrePersonne;

	private String nomPersonne;

	private String prenomPersonne;

	private String dateNaissancePersonne;

	private String nationalitePersonne;

	private String lieuScolariteTravail;
	
}
