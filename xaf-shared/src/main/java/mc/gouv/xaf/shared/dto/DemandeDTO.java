package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;

/**
 * Modélise une demande
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeDTO extends AbstractDemandeDTO {

	@Serial
    private static final long serialVersionUID = 877740374924793999L;

	protected DemandeCanalEnum canal;
    protected String creeParAgentId;
    protected transient DemandeCourrierDTO[] courriers;
    private Integer fkAccess;
    private Integer usagerId;
    private transient DemandeFileDTO[] fichiers;
    private String identifiant;
    private transient DemandeStatutDTO[] statuts;
    private transient DemandeStatutDTO dernierStatut;
    private transient DemandeDataDTO[] data;
    private transient DemandeComplementsDTO[] complements;
    private transient DemandeUsagerDTO usager;
    private String recapType;
    private transient DonneesMConnectDTO donneesMConnect;
    private SourceFiableDTO[] donneesCertifiees;
    private Integer pkDemandeSource;
    private Long modificationTimestamp;
    private transient JsonNode contenuInitial;
    private transient JsonNode meta;
    private transient DemandeAgentDTO agent;

    private TypeConnexionUsagerEnum typeConnexionUsager;

}
