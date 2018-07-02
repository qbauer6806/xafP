package mc.gouv.af.backweb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import mc.gouv.af.back.util.AfBackUtils;

/**
 * 
 * Sert à intercepter les requêtes vers les contrôleurs de page de la démarche afin notamment
 * de rajouter AfBackUtils automatiquement au ModelAndView pour toutes les pages.
 * 
 * @author qdeme
 *
 */
@Component
@Profile("gouv")
public class GouvInterceptor extends HandlerInterceptorAdapter {
    
    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            modelAndView.addObject("AfBackUtils", afBackUtils);
        }
        super.postHandle(request, response, handler, modelAndView);
    }

}
