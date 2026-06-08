package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AfDemandeExcelTemplateDTO extends AfDemandeExcelFlatDTO {

    private String etatInterne;
    private String demandeurDateNaissance;
    private String situationFamiliale;
    private String demandeurSituation;
    private String conjointDateNaissance;
    private String conjointSituation;
    private String personnesFoyerDateNaissance;
    private String personnesFoyerModeGarde;
    private String personnesFoyerSituation;
    private String personnesAscendantDateNaissance;
    private String personnesAscendantSituation;
    private String retourMonaco;
    private String logementActuelType;
    private String logementActuelSecteur;
    private String logementActuelComposition;
    private String logementActuelOccupation;
    private String locataireAide;
    private String bienImmobiliersProprietaire;
    private String numeroDossierArchive;

    public AfDemandeExcelTemplateDTO(DemandeFlatDTO generic) {
        super(generic);
    }
}
