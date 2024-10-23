package mc.gouv.xaf.backweb.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class IndexControllerImpl implements IndexControllerInterface {

    @Override
    public ModelAndView form() {
        return new ModelAndView("redirect:/index");
    }

    @Override
    public ModelAndView formIndex() {
        return new ModelAndView("index");
    }

}
