package mc.gouv.xaf.back.service.templates;
import java.util.Map;

public interface AfPropertiesTemplateProvider {

    Map<String, String> getModel(String propertyKey);
}
