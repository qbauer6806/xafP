package mc.gouv.xaf.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.context.annotation.Conditional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;

@Conditional(IndexationEnabledCondition.class)
@Entity
@Table(name = "DEM_RECHERCHE_CHAMP_CONFIG")
public class RechercheChampConfigBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "cle", nullable = false, unique = true)
    private String cle;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "editable", nullable = false)
    private boolean editable;

    @ManyToOne
    @JoinColumn(name = "fk_categorie")
    private RechercheCatConfigBo categorie;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public RechercheCatConfigBo getCategorie() {
        return categorie;
    }

    public void setCategorie(RechercheCatConfigBo categorie) {
        this.categorie = categorie;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

}
