package mc.gouv.xaf.back.service.itg.ulis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UlisObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public UlisObjectMapperProvider() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        this.mapper.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
