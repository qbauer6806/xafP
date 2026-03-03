package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise un brouillon d'une demande
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrouillonDTO {

    protected Integer pkBrouillons;

    private Integer fkAccess;

    private Integer usagerId;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateDerModif;

    protected JsonNode contenu;

    private BrouillonFileDTO[] fichiers;

    private String buildId;

    private String recapType;

    @ToString.Exclude
    private JsonNode meta;

    private DemandeStatutDTO dernierStatut;

    @ToString.Exclude
    private JsonNode contenuInitial;

}
