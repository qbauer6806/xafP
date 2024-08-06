package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.dsp.enums.common.ResidEtatEnum;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ResidStatutDemandeDTO implements Serializable {

    private static final long serialVersionUID = 1737364601317166129L;

    private String idTS;

    private ResidEtatEnum etat;
    
    // TODO Resid à supprimer car le timestamp sera connu de resid, mais pour le moment j'ai besoin d'un élément de comparaison
    private String timestamp;

}
