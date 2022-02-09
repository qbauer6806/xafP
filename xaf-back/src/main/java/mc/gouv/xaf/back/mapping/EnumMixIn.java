package mc.gouv.xaf.back.mapping;

import com.fasterxml.jackson.annotation.JsonValue;

public abstract class EnumMixIn {
    @JsonValue(false)
    abstract public String toValue();

    abstract public String toString();
}
