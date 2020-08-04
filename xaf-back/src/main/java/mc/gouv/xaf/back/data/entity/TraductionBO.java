package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;

/**
 * Classe BO de la table DEM_TRADUCTIONS
 * <p>
 * Cette table a été rejoutée au ticket #21240 pour trier les statuts des demandes en fontion de leur traduction.
 *
 * @author mboutelier.ext
 */
@Entity
@Table(name = "DEM_TRADUCTIONS")
public class TraductionBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_TRADUCTIONS", nullable = false)
    private Integer pkTraductions;

    @Column(name = "LANGUE", length = 32, nullable = false)
    private String langue;

    @Column(name = "CLE", length = 256, nullable = false)
    private String cle;

    @Column(name = "VALEUR", length = 256, nullable = false)
    private String valeur;

    public Integer getPkTraductions() {
        return pkTraductions;
    }

    public void setPkTraductions(Integer pkTraductions) {
        this.pkTraductions = pkTraductions;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

}
