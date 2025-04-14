package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente une locale dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureLocaleDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 9011144333200274364L;

    private String localeCode;

    private String localeLibelle;
    
    @Override
    public String toString() {
        return "NomenNomenclatureLocaleDTO [localeCode=" + localeCode + ", localeLibelle=" + localeLibelle + "]";
    }

}
