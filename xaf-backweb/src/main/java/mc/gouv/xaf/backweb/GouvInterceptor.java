package mc.gouv.xaf.backweb;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Sert à intercepter les requêtes vers les contrôleurs de page de la démarche afin notamment de rajouter AfBackUtils
 * automatiquement au ModelAndView pour toutes les pages.
 *
 * @author qdeme
 */
@Component
public class GouvInterceptor implements HandlerInterceptor {

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        if (modelAndView != null) {
            modelAndView.addObject("AfBackUtils", afBackUtils);
            modelAndView.addObject("DemarchesDataProvider", demarchesDataProvider);
        }
    }

}
