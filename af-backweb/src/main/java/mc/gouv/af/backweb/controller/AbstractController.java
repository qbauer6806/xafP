package mc.gouv.af.backweb.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import mc.gouv.af.back.util.AfBackUtils;

/**
 * 
 * Controller pour gérer entre autre les exceptions survenues sur les pages web
 * et pas les WS
 * 
 * @author fgaujous
 *
 */
public abstract class AbstractController extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		CustomDateEditor editor = new CustomDateEditor(AfBackUtils.sdf_JJ_MM_AAAA, true);
		binder.registerCustomEditor(Date.class, editor);
	}

	@Autowired
	private AfBackUtils afBackUtils;

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ModelAndView handleException(Exception ex) {
		LOGGER.error("Erreur interne", ex);
		ModelAndView mav = new ModelAndView("error/500");
		mav.addObject("AfBackUtils", afBackUtils);
		return mav;
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	public ModelAndView handleAccessDeniedException(AccessDeniedException ex) {
		LOGGER.error("Access Denied", ex);
		ModelAndView mav = new ModelAndView("error/403");
		mav.addObject("AfBackUtils", afBackUtils);
		return mav;
	}

}
