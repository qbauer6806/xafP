package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "dem_recherche_cat_config")
public class RechercheCatConfigBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "editable", nullable = false)
    private boolean editable;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<RechercheChampConfigBO> champs;

    public RechercheCatConfigBO() {
    }

    public RechercheCatConfigBO(String libelle, boolean editable) {
        this.libelle = libelle;
        this.editable = editable;
    }

}
