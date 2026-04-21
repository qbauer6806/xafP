package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class ConfigRechercheDTO {

    public enum ConfigRechercheOperand {
        AND,
        OR
    }

    private Set<String> buildIdsInclude;
    private Set<String> buildIdsExclude;
    private ConfigRechercheOperand operand = ConfigRechercheOperand.AND;

    public boolean isEmpty() {
        return (buildIdsInclude == null || buildIdsInclude.isEmpty()) && (buildIdsExclude == null
                || buildIdsExclude.isEmpty());
    }
}
