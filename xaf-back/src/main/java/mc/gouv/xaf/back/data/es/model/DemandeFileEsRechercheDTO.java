package mc.gouv.xaf.back.data.es.model;

import java.util.HashMap;
import java.util.Map;

public class DemandeFileEsRechercheDTO extends DemandeFileEsDTO {

    private Map<String, String> highlightedField = new HashMap<>();

    public Map<String, String> getHighlightedField() {
        return highlightedField;
    }

    public void setHighlightedField(Map<String, String> highlightedField) {
        this.highlightedField = highlightedField;
    }
}
