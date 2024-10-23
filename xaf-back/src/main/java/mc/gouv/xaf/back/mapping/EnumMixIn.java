package mc.gouv.xaf.back.mapping;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class EnumMixIn {

    @JsonValue(false)
    public abstract String toValue();

    @JsonValue
    public abstract String toString();
}
