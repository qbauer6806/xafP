package mc.gouv.af.data.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.context.annotation.Conditional;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;

@Conditional(IndexationEnabledCondition.class)
@Entity
@Table(name = "dem_recherche_cat_config")
public class RechercheCatConfigBo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "editable", nullable = false)
    private boolean editable;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RechercheChampConfigBo> champs;

    public RechercheCatConfigBo() {
    }

    public RechercheCatConfigBo(String libelle, boolean editable) {
        super();
        this.libelle = libelle;
        this.editable = editable;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Set<RechercheChampConfigBo> getChamps() {
        return champs;
    }

    public void setChamps(Set<RechercheChampConfigBo> champs) {
        this.champs = champs;
    }

}
