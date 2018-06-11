package mc.gouv.af.back.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import mc.gouv.af.back.util.AfBackUtils;

/**
 * Controller pour gérer entre autre les exceptions survenues sur les pages web et pas les ws
 * @author fgaujous
 *
 */
public abstract class AbstractController extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);
    
    @Autowired
    private AfBackUtils afBackUtils;

    public static final String URL_ERROR_500 = "error/500";
    public static final String URL_ERROR_403 = "error/403";

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        LOGGER.error("Erreur interne", ex);
        return URL_ERROR_500;
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        LOGGER.error("Access Denied", ex);
        return URL_ERROR_403;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        LOGGER.info("TESTTTT");
        modelAndView.addObject("AfBackUtils", afBackUtils);
        super.postHandle(request, response, handler, modelAndView);
    }
    
    

}
