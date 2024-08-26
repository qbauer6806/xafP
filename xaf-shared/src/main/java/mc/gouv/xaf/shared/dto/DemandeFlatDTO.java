package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise une demande simplifiée, à plat, à destination notamment de l'export Excel
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeFlatDTO {
    
    private Integer pkDemandes;
    
    private Integer usagerId;
    
    private String usagerPrenom;
    
    private String usagerNom;
    
    private String usagerEmail;

    private String dateCreation;

    private String langue;

    private String canal;

    private String observations;

    private String agentAffecteId;
    
    private String agentAffecteNom;

    private String dernierStatut;

    private String identifiant;

    private String courrierDateReception;

    private String courrierRefInterne;

    private String motif;

    private Map<String, String> marqueurs;

}
