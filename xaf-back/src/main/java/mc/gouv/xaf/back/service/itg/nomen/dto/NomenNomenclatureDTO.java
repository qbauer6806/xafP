package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente le retour d'un appel à l'API NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenNomenclatureDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2755866850453774444L;

    private String code;

    private String type;

    private String categorie;

    private String etat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateFin;

    private String version;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date versionDate;

    private String source;

    private String serviceResponsableCode;

    private String titre;

    private String description;

    private String remarques;

    private List<NomenNomenclatureLienDTO> nomenclatureLiens;

    private List<NomenNomenclatureLocaleDTO> nomenclatureLocales;

    private List<NomenNomenclatureParametreDTO> nomenclatureParametres;

    private List<NomenValeurDTO> valeurs;
    
    @Override
    public String toString() {
        return "NomenNomenclatureDTO [code=" + code + ", type=" + type + ", categorie=" + categorie + ", etat=" + etat
                + ", dateFin=" + dateFin + ", version=" + version + ", versionDate=" + versionDate + ", source="
                + source + ", serviceResponsableCode=" + serviceResponsableCode + ", titre=" + titre + ", description="
                + description + ", remarques=" + remarques + ", nomenclatureLiens=" + nomenclatureLiens
                + ", nomenclatureLocales=" + nomenclatureLocales + ", nomenclatureParametres=" + nomenclatureParametres
                + ", valeurs=" + valeurs + "]";
    }

}
