package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente un paramètre dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureParametreDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6999605900893785604L;

    private String parametreNom;

    private String parametreType;

    private boolean parametreObligatoire;

    private Integer parametreLongueur;

    private String parametreFormat;

    @Override
    public String toString() {
        return "NomenNomenclatureParametreDTO [parametreNom=" + parametreNom + ", parametreType=" + parametreType
                + ", parametreObligatoire=" + parametreObligatoire + ", parametreLongueur=" + parametreLongueur
                + ", parametreFormat=" + parametreFormat + "]";
    }

}
