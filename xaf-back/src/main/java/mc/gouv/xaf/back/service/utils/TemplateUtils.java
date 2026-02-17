package mc.gouv.xaf.back.service.utils;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.stereotype.Component;
import java.io.StringWriter;
import java.util.Map;

@Component
public final class TemplateUtils {

    private final VelocityEngine velocityEngine;

    private TemplateUtils(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public String renderWithVelocity(String template, Map<String, String> model) {
        if (template == null || template.isBlank() || model == null || model.isEmpty()) {
            return template;
        }

        VelocityContext ctx = new VelocityContext();
        model.forEach(ctx::put);

        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(ctx, writer, "properties-template", template);
        return writer.toString();
    }
}
