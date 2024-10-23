package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
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

    protected transient Map<String, String> marqueurs;

    protected String langue;

    protected String observations;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date courrierDateReception;

    protected String courrierRefInterne;

    protected Integer pkDemandes;

    @JsonIgnore
    protected boolean updated = false;

}
