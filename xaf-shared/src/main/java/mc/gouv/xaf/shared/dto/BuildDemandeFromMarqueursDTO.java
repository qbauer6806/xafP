package mc.gouv.xaf.shared.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BuildDemandeFromMarqueursDTO {

    private Map<String, String> donnees = new HashMap<>();

    private List<Map<String, String>> donneesTableaux = new ArrayList<>();

    private Map<String, List<String>> donneesChoixMultiple = new HashMap<>();


}
