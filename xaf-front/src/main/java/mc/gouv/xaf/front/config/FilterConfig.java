package mc.gouv.xaf.front.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * 
 * Correspond au paragraphe "Ajouter le filtre mc.gouv.tools.monitor.filters.MDCLogFilter dans le fichier web.xml" de
 * http://redmine/projects/tracabilite/wiki/Mise_aux_normes_des_applications
 * 
 * @author qdeme
 *
 */
public class FilterConfig implements ServletContextInitializer {

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        container.addFilter("MDCLogFilterAPI", MDCLogFilterAPI.class).addMappingForUrlPatterns(null, false, "/*");
    }

}
