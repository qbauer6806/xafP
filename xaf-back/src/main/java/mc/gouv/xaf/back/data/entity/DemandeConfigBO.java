package mc.gouv.xaf.back.data.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_CONFIG")
public class DemandeConfigBO {

    @Id
    @Column(name = "BUILD_ID", nullable = false)
    private String buildId;

    @Column(name = "CONTENU", columnDefinition = "JSONB", nullable = false)
    @Type(JsonType.class)
    private JsonNode contenu;

    @Column(name = "DERNIER_MODELE", nullable = false)
    private boolean dernierModele;

    @OneToMany(mappedBy = "buildId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<MarqueurBO> marqueurs;

}
