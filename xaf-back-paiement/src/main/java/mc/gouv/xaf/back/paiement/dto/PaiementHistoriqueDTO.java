package mc.gouv.xaf.back.paiement.dto;

import mc.gouv.xaf.back.paiement.enums.PaiementStatutEnum;

import java.time.LocalDateTime;

/**
 * Modélise le contenu d'une ligne d'historique
 *
 * @author qdeme
 */
public class PaiementHistoriqueDTO {

    private Integer pkHistorique;

    private Integer fkDemandes;

    private LocalDateTime date;

    private PaiementStatutEnum statut;

    private String couleur;

    private Integer usagerId;

    private String contenu;

    public Integer getPkHistorique() {
        return pkHistorique;
    }

    public void setPkHistorique(Integer pkHistorique) {
        this.pkHistorique = pkHistorique;
    }

    public Integer getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(Integer fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public PaiementStatutEnum getStatut() {
        return statut;
    }

    public void setStatut(PaiementStatutEnum statut) {
        this.statut = statut;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    @Override
    public String toString() {
        return "PaiementHistoriqueDTO{" +
                "pkHistorique=" + pkHistorique +
                ", fkDemandes=" + fkDemandes +
                ", date=" + date +
                ", statut=" + statut +
                ", usagerId=" + usagerId +
                ", contenu='" + contenu + '\'' +
                '}';
    }

}
