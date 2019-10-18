#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.backserver.controller;

import mc.gouv.xaf.backweb.controller.AbstractController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page /
 * 
 * @author mpavone
 *
 */
@Controller
@RequestMapping("/")
public class IndexController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView form() {

        LOGGER.info("======================= Appel de la page /");

        LOGGER.info("======================= Fin /");

        ModelAndView mav = new ModelAndView("redirect:/index");
        return mav;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/index")
    public ModelAndView formIndex() {

        LOGGER.info("======================= Appel de la page /index");

        LOGGER.info("======================= Fin /index");

        ModelAndView mav = new ModelAndView("index");
        return mav;
    }

}
