package mc.gouv.xaf.back.service.itg.ulis;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class UlisOffsetDateTimeSerializer extends StdSerializer<OffsetDateTime> {

    private static final DateTimeFormatter ULIS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(java.time.ZoneOffset.UTC);

    public UlisOffsetDateTimeSerializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator gen, SerializationContext ctxt) {
        gen.writeString(ULIS_FORMAT.format(value));
    }
}
