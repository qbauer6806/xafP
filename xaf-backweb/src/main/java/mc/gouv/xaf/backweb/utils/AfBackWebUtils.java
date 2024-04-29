package mc.gouv.xaf.backweb.utils;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * Classe utilitaire pour le projet xaf-back
 *
 * @author qdeme
 */
@Component
public class AfBackWebUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackWebUtils.class);

    private static String envName;

    private static String envColor;


    @Autowired
    @Lazy
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    @PostConstruct
    public void postConstructEnv() {
        String env = gouvPropertiesResolver.getGouvSharedEnv();
        String color = gouvPropertiesResolver.getGouvSharedEnvColor();
        setEnvironmentNameAndColor(env, color);
    }

    private static void setEnvironmentNameAndColor(String env, String color) {
        // Si production, ne rien afficher
        if ("prod".equals(env)) {
            envName = "";
        } else if ("sup".equals(env)) {
            envName = "Support";
        } else if ("pre".equals(env)) {
            envName = "Pré-production";
        } else if ("rec".equals(env)) {
            envName = "Recette";
        } else if ("dev".equals(env)) {
            envName = "Développement";
        } else if ("loc".equals(env)) {
            envName = "Local";
        } else {
            envName = "Environnement inconnu";
        }

        // Fond noir si environnement de production, et non pas rouge
        if ("prod".equals(env)) {
            envColor = "#000000";
        } else {
            envColor = color;
        }
    }
}
