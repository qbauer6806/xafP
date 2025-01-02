package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AbstractDemandeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4591114018832121128L;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateDerModif;

    protected transient JsonNode contenu;

    protected transient JsonNode contenuTrad;

    protected transient JsonNode config;

    protected transient Map<String, Object> marqueurs;
    protected transient Map<String, Object> marqueursTrad;

    protected String langue;

    protected String observations;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date courrierDateReception;

    protected String courrierRefInterne;

    protected Integer pkDemandes;

    @JsonIgnore
    protected boolean updated = false;

    public String getMarqueur(String marqueurId) {
        return getMap(marqueurs, marqueurId);
    }

    public String getMarqueurTrad(String marqueurId) {
        return getMap(marqueursTrad, marqueurId);
    }

    private String getMap(Map<String, Object> map, String marqueurId) {
        if (map != null && map.get(marqueurId) instanceof String str) {
            return str;
        }
        return null;
    }

    public List<Map<String, String>> getMarqueurTableau(String marqueurId) {
        if (marqueurs != null && marqueurs.get(marqueurId) instanceof List list) {
            if (!list.isEmpty() && list.getFirst() instanceof Map) {
                return list;
            }
        }
        return null;
    }

    public List<String> getMarqueurChoixMultiple(String marqueurId) {
        if (marqueurs != null && marqueurs.get(marqueurId) instanceof List list) {
            if (!list.isEmpty() && list.getFirst() instanceof String) {
                return list;
            }
        }
        return null;
    }


}
