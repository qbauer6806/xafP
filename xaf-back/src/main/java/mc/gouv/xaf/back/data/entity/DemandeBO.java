package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * Classe BO de la table DEM.DEMANDES
 * 
 * Attention ! À chaque ajout de Set<> dans ce BO, penser à mettre à jour l'algorithme de duplication de demandes pour
 * les prendre en compte. Et mettre à jour les transformers pour toute donnée ajoutée.
 * 
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_DEMANDES")
public class DemandeBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDES", nullable = false)
    private Integer pkDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ACCESS")
    private AccessBO fkAccess;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

    @Column(name = "CONTENU", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String contenu;

    @Column(name = "LANGUE", length = 2, nullable = true)
    @Size(min = 0, max = 2)
    private String langue;

    @Column(name = "CANAL", length = 30, nullable = false)
    @Size(min = 0, max = 30)
    private String canal;

    @Column(name = "OBSERVATIONS", length = 10000, nullable = true)
    @Size(min = 0, max = 10000)
    private String observations;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dateCreation DESC")
    private Set<DemandesComplementsBO> demandesComplements;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesFilesBO> files;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesStatutsBO> statuts;

    @Column(name = "AGENT_AFFECTE_ID", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String agentAffecteId;

    @Column(name = "CREE_PAR_AGENT_ID", length = 128, nullable = true)
    @Size(min = 0, max = 128)
    private String creeParAgentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DERNIER_STATUT")
    private DemandesStatutsBO dernierStatut;

    @Column(name = "IDENTIFIANT", length = 30, nullable = false)
    @Size(min = 1, max = 30)
    private String identifiant;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesDataBO> data;

    @Column(name = "COURRIER_DATE_RECEPTION", nullable = true)
    private Date courrierDateReception;

    @Column(name = "COURRIER_REF_INTERNE", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String courrierRefInterne;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesCourriersBO> courriers;
    
    @Column(name = "USAGER_NOM", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String usagerNom;
    
    @Column(name = "USAGER_PRENOM", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String usagerPrenom;
    
    @Column(name = "USAGER_EMAIL", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String usagerEmail;
    
    @Column(name = "BUILD_ID", length = 32, nullable = true)
    @Size(min = 0, max = 32)
    private String buildId;
    
    @Column(name = "RECAP_TYPE", length = 256, nullable = true)
    @Size(min = 0, max = 256)
    private String recapType;
    
    @Column(name = "DONNEES_CERTIFIEES", columnDefinition = "TEXT", nullable = true)
    private String donneesCertifiees;
    
    // De type Integer et non DemandeBO (autrement dit : pas de foreign key en base)
    // Ceci afin d'être tranquille le jour où cette demande source doit être purgée (supprimée)
    @Column(name = "PK_DEMANDE_SOURCE", nullable = true)
    private Integer pkDemandeSource;

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

    public AccessBO getFkAccess() {
        return fkAccess;
    }

    public void setFkAccess(AccessBO fkAccess) {
        this.fkAccess = fkAccess;
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Set<DemandesComplementsBO> getDemandesComplements() {
        return demandesComplements;
    }

    public void setDemandesComplements(Set<DemandesComplementsBO> demandesComplements) {
        this.demandesComplements = demandesComplements;
    }

    public Set<DemandesFilesBO> getFiles() {
        return files;
    }

    public void setFiles(Set<DemandesFilesBO> files) {
        this.files = files;
    }

    public Set<DemandesStatutsBO> getStatuts() {
        return statuts;
    }

    public void setStatuts(Set<DemandesStatutsBO> statuts) {
        this.statuts = statuts;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getAgentAffecteId() {
        return agentAffecteId;
    }

    public void setAgentAffecteId(String agentAffecteId) {
        this.agentAffecteId = agentAffecteId;
    }

    public DemandesStatutsBO getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(DemandesStatutsBO dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public Set<DemandesDataBO> getData() {
        return data;
    }

    public void setData(Set<DemandesDataBO> data) {
        this.data = data;
    }

    public Date getCourrierDateReception() {
        return courrierDateReception;
    }

    public void setCourrierDateReception(Date courrierDateReception) {
        this.courrierDateReception = courrierDateReception;
    }

    public String getCourrierRefInterne() {
        return courrierRefInterne;
    }

    public void setCourrierRefInterne(String courrierRefInterne) {
        this.courrierRefInterne = courrierRefInterne;
    }

    public Set<DemandesCourriersBO> getCourriers() {
        return courriers;
    }

    public void setCourriers(Set<DemandesCourriersBO> courriers) {
        this.courriers = courriers;
    }

    public String getCreeParAgentId() {
        return creeParAgentId;
    }

    public void setCreeParAgentId(String creeParAgentId) {
        this.creeParAgentId = creeParAgentId;
    }

    public String getUsagerNom() {
        return usagerNom;
    }

    public void setUsagerNom(String usagerNom) {
        this.usagerNom = usagerNom;
    }
    
    public String getUsagerPrenom() {
        return usagerPrenom;
    }
    
    public void setUsagerPrenom(String usagerPrenom) {
        this.usagerPrenom = usagerPrenom;
    }

    public String getUsagerEmail() {
        return usagerEmail;
    }

    public void setUsagerEmail(String usagerEmail) {
        this.usagerEmail = usagerEmail;
    }

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getRecapType() {
		return recapType;
	}

	public void setRecapType(String recapType) {
		this.recapType = recapType;
	}

	public String getDonneesCertifiees() {
		return donneesCertifiees;
	}

	public void setDonneesCertifiees(String donneesCertifiees) {
		this.donneesCertifiees = donneesCertifiees;
	}

	public Integer getPkDemandeSource() {
		return pkDemandeSource;
	}

	public void setPkDemandeSource(Integer pkDemandeSource) {
		this.pkDemandeSource = pkDemandeSource;
	}

}
