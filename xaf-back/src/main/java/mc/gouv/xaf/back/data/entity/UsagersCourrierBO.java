package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.USAGERS_COURRIER
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_USAGERS_COURRIER")
public class UsagersCourrierBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_USAGERSCOURRIER", nullable = false)
    private Integer pkUsagersCourrier;

    @Column(name = "FK_DEMARCHEID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String demarcheId;

    @Column(name = "LOGIN", length = 20, nullable = false)
    @NotBlank
    @Size(min = 1, max = 20)
    private String login;

    @Column(name = "TITRE", nullable = true)
    private Integer titre;

    @Column(name = "NOM", length = 50, nullable = true)
    @Size(min = 0, max = 50)
    private String nom;

    @Column(name = "PRENOM", length = 20, nullable = true)
    @Size(min = 0, max = 20)
    private String prenom;

    @Column(name = "RAISON_SOCIALE", length = 100, nullable = true)
    @Size(min = 0, max = 100)
    private String raisonSociale;

    @Column(name = "ADRESSE1", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String adresse1;

    @Column(name = "ADRESSE2", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String adresse2;

    @Column(name = "ADRESSE_COMPLEMENT", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String adresseComplement;

    @Column(name = "CODE_POSTAL", length = 10, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10)
    private String codePostal;

    @Column(name = "VILLE", length = 50, nullable = false)
    @NotBlank
    @Size(min = 1, max = 50)
    private String ville;

    @Column(name = "PAYS", length = 2, nullable = false)
    @NotBlank
    @Size(min = 2, max = 2)
    private String pays;

    @Column(name = "TELEPHONE", length = 64, nullable = true)
    @Size(min = 0, max = 64)
    private String telephone;

    @Column(name = "EMAIL", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String email;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

    public Integer getPkUsagersCourrier() {
        return pkUsagersCourrier;
    }

    public void setPkUsagersCourrier(Integer pkUsagersCourrier) {
        this.pkUsagersCourrier = pkUsagersCourrier;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getTitre() {
        return titre;
    }

    public void setTitre(Integer titre) {
        this.titre = titre;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public String getAdresse2() {
        return adresse2;
    }

    public void setAdresse2(String adresse2) {
        this.adresse2 = adresse2;
    }

    public String getAdresseComplement() {
        return adresseComplement;
    }

    public void setAdresseComplement(String adresseComplement) {
        this.adresseComplement = adresseComplement;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

}
