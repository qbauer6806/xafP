package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "DEM_RECHERCHE_CHAMP_CONFIG")
public class RechercheChampConfigBO {

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
    private RechercheCatConfigBO categorie;

}
