package mc.gouv.xaf.back.mapping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class PaysMixIn {
    @JsonSerialize(using = PaysSerializer.class)
    public String pays;
}
