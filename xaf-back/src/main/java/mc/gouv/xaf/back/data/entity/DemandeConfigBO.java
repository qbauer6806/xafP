package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_CONFIG")
public class DemandeConfigBO {

    @Id
    @Column(name = "BUILD_ID", nullable = false)
    private String buildId;

    @Column(name = "CONTENU", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode contenu;

    @Column(name = "VERSION", length = 128)
    @Size(max = 128)
    private String version;

    @OneToMany(mappedBy = "buildId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<MarqueurBO> marqueurs;

}
