package mc.gouv.xaf.xaf12batch.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

}
