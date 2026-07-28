package mc.gouv.xaf.back.data.projection;

import tools.jackson.databind.JsonNode;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;

@Getter
@AllArgsConstructor
public class DemandeExportDTO {

    private Integer pkDemandes;
    private Date dateCreation;
    private Date dateDerModif;
    private Date courrierDateReception;
    private JsonNode contenu;
    private JsonNode contenuTrad;
    private String langue;
    private String canal;
    private String observations;
    private String courrierRefInterne;
    private DemandesAgentsBO agent;
    private DemandesUsagersBO usager;
    private DemandeConfigBO config;
    private DemandesStatutsBO dernierStatut;
    private String identifiant;
}
