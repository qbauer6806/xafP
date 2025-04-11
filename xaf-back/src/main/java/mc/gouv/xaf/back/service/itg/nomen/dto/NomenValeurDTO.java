package mc.gouv.xaf.back.service.itg.nomen.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Représente une valeur dans une nomenclature NOMEN
 * 
 * @author qdeme
 * 
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NomenValeurDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5766119000361740295L;

    private String code;

    private String etat;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateDebut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateFin;

    private String libelleCourt;

    private String libelleLong;

    private Integer ordre;

    private List<NomenValeurValeurLienDTO> valeurLiens;

    private List<NomenValeurValeurParametreDTO> valeurParametres;

    @Override
    public String toString() {
        return "NomenValeurDTO [code=" + code + ", etat=" + etat + ", dateDebut=" + dateDebut + ", dateFin=" + dateFin
                + ", libelleCourt=" + libelleCourt + ", libelleLong=" + libelleLong + ", ordre=" + ordre
                + ", valeurLiens=" + valeurLiens + ", valeurParametres=" + valeurParametres + "]";
    }

}
