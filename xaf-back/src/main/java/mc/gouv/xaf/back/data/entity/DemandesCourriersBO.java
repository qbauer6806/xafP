package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.DEMANDES_COURRIERS
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES_COURRIERS")
public class DemandesCourriersBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOURRIERS", nullable = false)
    private Integer pkDemandesCourriers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDESSTATUTS")
    private DemandesStatutsBO fkDemandesStatuts;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "NAME", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String name;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "META", length = 512, nullable = true)
    @Size(min = 0, max = 512)
    private String meta;

    @Column(name = "DATE_PRINTED", nullable = true)
    private Date datePrinted;

    @Column(name = "IDENTIFIANT", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String identifiant;

    public Integer getPkDemandesCourriers() {
        return pkDemandesCourriers;
    }

    public void setPkDemandesCourriers(Integer pkDemandesCourriers) {
        this.pkDemandesCourriers = pkDemandesCourriers;
    }

    public DemandeBO getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(DemandeBO fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public DemandesStatutsBO getFkDemandesStatuts() {
        return fkDemandesStatuts;
    }

    public void setFkDemandesStatuts(DemandesStatutsBO fkDemandesStatuts) {
        this.fkDemandesStatuts = fkDemandesStatuts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Date getDatePrinted() {
        return datePrinted;
    }

    public void setDatePrinted(Date datePrinted) {
        this.datePrinted = datePrinted;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

}
