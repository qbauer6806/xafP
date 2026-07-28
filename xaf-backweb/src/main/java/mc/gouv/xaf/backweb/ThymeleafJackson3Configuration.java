package mc.gouv.xaf.backweb;

import java.io.Writer;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.serializer.IStandardJavaScriptSerializer;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configure Thymeleaf pour utiliser Jackson 3 lors de l'inlining JavaScript.
 *
 * <p>Thymeleaf 3.1.5 (embarqué par Spring Boot 4) ne connaît que Jackson 2 : il
 * sérialise un {@link tools.jackson.databind.JsonNode} (Jackson 3) comme un POJO
 * ({@code empty}, {@code object}, {@code nodeType}...) au lieu d'un arbre JSON.
 * On remplace donc le {@link IStandardJavaScriptSerializer} du dialecte standard
 * par un serializer adossé au {@link JsonMapper} Jackson 3 de l'application.</p>
 *
 * <p>Pour plus d'informations :
 * <a href="https://github.com/thymeleaf/thymeleaf/issues/1056">thymeleaf/thymeleaf#1056</a></p>
 */
@Configuration
public class ThymeleafJackson3Configuration {

    /**
     * Remplace le serializer JS du dialecte standard par un serializer Jackson 3,
     * via un {@link BeanPostProcessor} sur le {@link SpringTemplateEngine} déjà
     * auto-configuré par Boot.
     *
     * <p>On ne redéfinit pas le bean {@code templateEngine} : {@code setDialect(...)}
     * effacerait tous les autres dialectes, dont Spring Security qui fournit
     * {@code #authentication} (sinon {@code null} → {@code EL1011E}). Ici, seul le
     * serializer change ; les autres dialectes et la config Boot restent intacts.</p>
     */
    @Bean
    static BeanPostProcessor springTemplateEngine(ObjectProvider<JsonMapper> jsonMapper) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
                if (bean instanceof SpringTemplateEngine engine) {
                    IStandardJavaScriptSerializer serializer =
                            new Jackson3ThymeleafJavaScriptSerializer(jsonMapper.getObject());
                    engine.getDialects().stream()
                            .filter(StandardDialect.class::isInstance)
                            .map(StandardDialect.class::cast)
                            .forEach(d -> d.setJavaScriptSerializer(serializer));
                }
                return bean;
            }
        };
    }

    /**
     * {@link IStandardJavaScriptSerializer} adossé au {@link JsonMapper} Jackson 3.
     *
     * <p>Permet à Thymeleaf de sérialiser correctement les {@code tools.jackson.*}
     * (dont {@link tools.jackson.databind.JsonNode}) en réutilisant le mapper de
     * l'application, sans réintroduire Jackson 2.</p>
     */
    public static class Jackson3ThymeleafJavaScriptSerializer implements IStandardJavaScriptSerializer {

        private final JsonMapper jsonMapper;

        public Jackson3ThymeleafJavaScriptSerializer(JsonMapper jsonMapper) {
            this.jsonMapper = jsonMapper.rebuild()
                    .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                    .build();
        }

        @Override
        public void serializeValue(Object object, Writer writer) {
            jsonMapper.writeValue(writer, object);
        }
    }
}
