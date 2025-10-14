package mc.gouv.xaf.backweb.controller;

import java.util.ArrayList;
import java.util.List;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour les fonctionnalites (onglets) Utilisateurs et Parametres
 *
 * @author tverdoyan
 */
@Controller
@Secured({ "ROLE_PARAMETRAGE", "ROLE_CONFIGURATION" })
@RequestMapping("/gestion/utilisateurs")
public class GestionUtilisateursController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionUtilisateursController.class);

    @Autowired
    private UtilisateursCache utilisateursCache;

    @GetMapping
    public ModelAndView formUser(Model model) {

        LOGGER.info("Appel de la page /gestion/utilisateurs. Méthode formUser");
        List<User> list = new ArrayList<>(utilisateursCache.getAll().values());

        model.addAttribute("userList", list);
        ModelAndView mav = new ModelAndView("gestion/utilisateurs/utilisateurs");

        LOGGER.info("======================= Fin /gestion/utilisateurs. Méthode formUser");

        return mav;
    }
}
