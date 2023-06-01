package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;

/**
 * Représentation BO de la table DEM.TACHES
 * <br>
 * Une tâche est un objet relié à une demande, elle permet de représenter la fonctionnalité de la validation partielle pour les démarches ayant plusieurs élements comme DUPECIM.
 * <br>
 * Une tâche est composée des éléments suivants :
 * <li>Une FK vers la demande concernée</li>
 * <li>Un statut pour les agents</li>
 * <li>Un statut pour les agents valideur</li>
 * <li>Un motif</li>
 * <li>Un commentaire</li>
 * <li>Un type</li>
 * <li>Un contenu pour des éléments custom par déarches</li>
 * <li>Un flag "locked" permettant d'indiquer que la tâche n'est plus éditable</li>
 *
 * @author mboutelier.ext
 */
@Entity
@Table(name = "DEM_TACHES")
public class TacheBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_TACHES", nullable = false)
    private Integer pkTaches;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES", nullable = false)
    private DemandeBO demande;

    @Column(name = "CODE_STATUT_AGENT", length = 128)
    private String codeStatutAgent;

    @Column(name = "CODE_STATUT_VALIDEUR", length = 128)
    private String codeStatutValideur;

    @Column(name = "CODE_MOTIF", length = 128)
    private String codeMotif;

    @Column(name = "CODE_TYPE", length = 128)
    private String codeType;

    @Column(name = "COMMENTAIRE", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "CONTENU", columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "LOCKED", columnDefinition = "BOOLEAN")
    private boolean locked;

    public Integer getPkTaches() {
        return pkTaches;
    }

    public void setPkTaches(Integer pkTaches) {
        this.pkTaches = pkTaches;
    }

    public DemandeBO getDemande() {
        return demande;
    }

    public void setDemande(DemandeBO demande) {
        this.demande = demande;
    }

    public String getCodeStatutAgent() {
        return codeStatutAgent;
    }

    public void setCodeStatutAgent(String codeStatutAgent) {
        this.codeStatutAgent = codeStatutAgent;
    }

    public String getCodeStatutValideur() {
        return codeStatutValideur;
    }

    public void setCodeStatutValideur(String codeStatutValideur) {
        this.codeStatutValideur = codeStatutValideur;
    }

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
