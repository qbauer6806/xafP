package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise un statut d'une demande
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@ToString
public class DemandeStatutDTO {

    private Integer pkStatut;

    private String libelle;
    private String name;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private String agentId;

    private Integer usagerId;

    private String codeMotif;

    private String commentaire;

    private String texteAEnvoyer;

}
