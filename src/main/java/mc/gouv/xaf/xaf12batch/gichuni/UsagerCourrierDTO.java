package mc.gouv.xaf.xaf12batch.gichuni;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UsagerCourrierDTO {

    private Integer pkUsagersCourrier;

    private String demarcheId;

    private String login;

    private Integer titre;

    private String nom;

    private String prenom;

    private String raisonSociale;

    private String adresse1;

    private String adresse2;

    private String adresseComplement;

    private String codePostal;

    private String ville;

    private String pays;

    private String telephone;

    private String email;
    
    // Le contenu de l'access à créer dans le cas de la création d'un usager courrier
    private JsonNode accessContenu;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;
    
    @JsonIgnore
    private boolean updated = false;
    
    private int nbDemandes;

}
