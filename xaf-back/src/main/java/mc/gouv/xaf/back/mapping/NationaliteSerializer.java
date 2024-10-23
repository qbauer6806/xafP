package mc.gouv.xaf.back.mapping;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NationaliteSerializer extends JsonSerializer<String> {

    @Autowired
    private PaysCache paysCache;

    @Override
    public void serialize(String paysCode, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeString(StringUtils.isBlank(paysCode) ? " " : paysCache.getNationalite(paysCode, "fr"));
    }
}
