package mc.gouv.xaf.backweb.web.config.error;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;

/**
 * Controller pour les pages d'erreur
 * 
 * @author qdeme
 * 
 */
@Controller
@RequestMapping("/error")
// https://github.com/spring-projects/spring-boot/issues/5638
public class BackErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BackErrorController.class);

    public static final String URL_ERROR_403 = "error/403";
    public static final String URL_ERROR_404 = "error/404";
    public static final String URL_ERROR_500 = "error/500";
    
    @Autowired
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    @GetMapping(path = "/403")
    public ModelAndView error403(Model model, HttpServletRequest request) {
    	return getModelAndViewForError(403, request);
    }

    @GetMapping(path = "/404")
    public ModelAndView error404(Model model, HttpServletRequest request) {
    	return getModelAndViewForError(404, request);
    }

    @GetMapping(path = "/500")
    public ModelAndView error500(Model model, HttpServletRequest request) {
    	return getModelAndViewForError(500, request);
    }
    
    private ModelAndView getModelAndViewForError(Integer errCode, HttpServletRequest request) {
    	ModelAndView mav = new ModelAndView();
    	if (errCode == 403) {
    		mav.setViewName(URL_ERROR_403);
    	}
    	else if (errCode == 404) {
    		mav.setViewName(URL_ERROR_404);
    	}
    	else if (errCode == 500) {
    		mav.setViewName(URL_ERROR_500);
    	}
    	
    	String env = gouvPropertiesResolver.getGouvSharedEnv();
    	
    	mav.addObject("tsCode", gouvPropertiesResolver.getDemarcheId());
    	mav.addObject("environnement", env);
    	mav.addObject("matricule", AfBackUtils.getAuthenticatedAgentId());
    	mav.addObject("errCode", errCode);
    	DateFormat dateFormat = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_HOURS_MINUTES_SECONDS_FORMAT);
    	mav.addObject("errDate", dateFormat.format(new Date()));
    	
    	// Génération et affichage dans les logs du code d'erreur remonté à l'utilisateur
    	String errId = "ERRTS" + System.currentTimeMillis();
    	LOGGER.error("Code d'erreur affiché à l'utilisateur : {}", errId);
    	mav.addObject("errId", errId);
    	
    	// Affichage de la stacktrace à l'utilisateur si l'on n'est pas en production
    	// La prod doit avoir un environnement vide !
    	if (errCode == 500 && !StringUtils.isBlank(env)) {
	    	Exception e = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
	    	if (e != null) {
	    		mav.addObject("stacktrace", ExceptionUtils.getStackTrace(e));
	    	}
    	}
    	
    	return mav;
    }

}
