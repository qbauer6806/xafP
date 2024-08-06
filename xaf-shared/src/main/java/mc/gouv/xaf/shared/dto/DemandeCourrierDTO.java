package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise un courrier généré pour une demande
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class DemandeCourrierDTO {

    private Integer pkCourrier;
    
    private Integer demandeId;
    
    private DemandeStatutDTO fkStatut;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    
    private String name;
    
    private String url;
    
    private String meta;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date datePrinted;
    
    private String identifiant;
    
    private String demandeIdentifiant;

}
