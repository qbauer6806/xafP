package mc.gouv.xaf.backweb.controller;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page d'accueil /
 *
 * @author qdeme
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    private final IndexControllerInterface indexControllerInterface;

    @GetMapping
    public ModelAndView form() {

        LOGGER.info("======================= Appel de la page /");

        ModelAndView mav = indexControllerInterface.form();

        LOGGER.info("======================= Fin /");
        return mav;
    }

    @GetMapping(value = "/index")
    public ModelAndView formIndex() {

        LOGGER.info("======================= Appel de la page /index");

        ModelAndView mav = indexControllerInterface.formIndex();
        mav.addObject("agentId", AfBackUtils.getAuthenticatedAgentId());

        LOGGER.info("======================= Fin /index");
        return mav;
    }

}
