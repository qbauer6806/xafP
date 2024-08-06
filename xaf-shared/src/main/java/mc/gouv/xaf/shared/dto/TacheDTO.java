package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;

/**
 * @author mboutelier.ext
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TacheDTO {

    @Setter
    private Integer pkTaches;

    @Setter
    private Integer fkDemande;

    private StatutTachesEnum statutAgent;

    private StatutTachesEnum statutValideur;

    @Setter
    private String codeMotif;

    @Setter
    private String codeType;

    @Setter
    private String commentaire;

    @Setter
    private JsonNode contenu;

    @Setter
    private boolean locked;

    public void setStatutAgent(String codeStatutAgent) {
        if (null != codeStatutAgent) {
            this.statutAgent = StatutTachesEnum.valueOf(codeStatutAgent);
        }
    }

    public void setStatutValideur(String codeStatutValideur) {
        this.statutValideur = null != codeStatutValideur ? StatutTachesEnum.valueOf(codeStatutValideur) : null;
    }

}
