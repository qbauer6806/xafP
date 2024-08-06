package mc.gouv.xaf.front.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

import java.io.Serializable;

/**
 * 
 * Classe contenant les informations de l'usager dans xaf-servlet
 * 	- Informations issues de GICHUNI (par héritage de xaf-shared)
 *  - Informations pour la session de l'usager
 *  	- Infos de Tokens GICHKEY
 *  	- Infos certifiées / MConnect
 *  	- accessId
 *  	- ...
 * 
 * @author qdeme
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagerInfosDTO extends GichuniUsagerDTO implements Serializable {

    private static final long serialVersionUID = -7219376931453637516L;

    // gender identifiers and labels
    public static final short GENDER_MR_INDEX = 0;
    public static final short GENDER_MME_INDEX = 1;
    public static final short GENDER_MLLE_INDEX = 2;

    protected boolean isUsagerCourrier = false;
    
    @Setter
    @Getter
    protected Integer accessId;
    
    @Setter
    @Getter
    protected KeycloakTokenInfo tokenInfo;
    
    @Setter
    @Getter
    protected boolean mConnect = false;

    public String getTitreLabel() {
        if (getTitre() == null) {
            return null;
        }
        return switch (getTitre()) {
            case GENDER_MR_INDEX -> "Monsieur";
            case GENDER_MME_INDEX -> "Madame";
            case GENDER_MLLE_INDEX -> "Mademoiselle";
            default -> null;
        };
    }

    @Override
    public String toString() {
        return "UsagerReadOnlyBean [id=" + getId() + ", login=" + getLogin() + "]";
    }

    @JsonProperty("isUsagerCourrier")
    public boolean isUsagerCourrier() {
        return isUsagerCourrier;
    }

    @JsonProperty("isUsagerCourrier")
    public void setUsagerCourrier(boolean isUsagerCourrier) {
        this.isUsagerCourrier = isUsagerCourrier;
    }

}
