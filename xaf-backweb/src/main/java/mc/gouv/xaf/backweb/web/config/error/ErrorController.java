package mc.gouv.xaf.backweb.web.config.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller pour la page /form
 * 
 * @author bbois
 * 
 */
@Controller
@RequestMapping("/error")
// https://github.com/spring-projects/spring-boot/issues/5638
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @Autowired
    Environment env;

    public static final String URL_ERROR_403 = "error/403";
    public static final String URL_ERROR_404 = "error/404";
    public static final String URL_ERROR_500 = "error/500";

    @RequestMapping(path = "/403")
    public String error403(Model model) {

        return URL_ERROR_403;
    }

    @RequestMapping(path = "/404")
    public String error404(Model model) {

        return URL_ERROR_404;
    }

    @RequestMapping(path = "/500")
    public String error500(Model model) {
        return URL_ERROR_500;
    }

    // Update Spring 2.5.9, getErroPath n'est plus dans l'interface
    //@Override
    public String getErrorPath() {
        return "/__dummyErrorPath";
    }
}
