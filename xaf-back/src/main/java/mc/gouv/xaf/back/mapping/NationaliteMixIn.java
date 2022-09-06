package mc.gouv.xaf.back.mapping;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class NationaliteMixIn {
    @JsonSerialize(using = NationaliteSerializer.class)
    public String nationalite;
}
