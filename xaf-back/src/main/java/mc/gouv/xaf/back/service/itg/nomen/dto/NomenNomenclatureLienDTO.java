package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un lien dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureLienDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4932647062473486473L;

    private String lienCode;

    private String lienDescription;

    @Override
    public String toString() {
        return "NomenNomenclatureLienDTO [lienCode=" + lienCode + ", lienDescription=" + lienDescription + "]";
    }

}
