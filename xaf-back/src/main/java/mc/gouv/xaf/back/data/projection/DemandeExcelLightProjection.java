package mc.gouv.xaf.back.data.projection;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DemandeExcelLightProjection {

    private Integer pkDemandes;
    private String identifiant;
    private Date dateCreation;
    private String etatInterne;
    private String agentAffecteNom;
    private String canal;
    private String courrierRefInterne;
    private Date courrierDateReception;
    private String demandeurDateNaissance;
    private String situationFamiliale;
    private String demandeurSituation;
    private String conjointDateNaissance;
    private String conjointSituation;
    private String personnesFoyerJson;
    private String personnesAscendantJson;
    private String retourMonaco;
    private String logementActuelType;
    private String logementActuelSecteur;
    private String logementActuelComposition;
    private String logementActuelOccupation;
    private String locataireAide;
    private String bienImmobiliersProprietaire;
}
