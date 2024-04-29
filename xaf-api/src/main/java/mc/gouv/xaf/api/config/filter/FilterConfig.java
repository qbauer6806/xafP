package mc.gouv.xaf.api.config.filter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import mc.gouv.xaf.back.config.filter.MDCLogFilterAPI;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import mc.gouv.xaf.api.config.filter.http.HttpMethodFilter;

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
        container.addFilter("HttpMethodFiler", HttpMethodFilter.class).addMappingForUrlPatterns(null, false, "/*");
    }

}
