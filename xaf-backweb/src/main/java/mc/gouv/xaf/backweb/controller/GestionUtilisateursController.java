package mc.gouv.xaf.backweb.controller;

import mc.gouv.logon.apiclient.LogonApiClient;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller pour les fonctionnalites (onglets) Utilisateurs et Parametres
 *
 * @author tverdoyan
 */
@Controller
@Secured({"ROLE_PARAMETRAGE", "ROLE_CONFIGURATION"})
@RequestMapping("/gestion/utilisateurs")
public class GestionUtilisateursController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionUtilisateursController.class);
    @Autowired
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    @GetMapping
    public ModelAndView formUser(Model model) {

        LOGGER.info("Appel de la page /gestion/utilisateurs. Méthode formUser");
        List<User> list = new ArrayList<>();

        try {
            LogonApiClient logonApiClient = new LogonApiClient(gouvPropertiesResolver.getGouvSharedLogonRestUrl());
            list = logonApiClient.getRessUser().getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formUser. Msg : {}", e.getMessage(), e);
        }

        model.addAttribute("userList", list != null ? list : new ArrayList<>());
        ModelAndView mav = new ModelAndView("gestion/utilisateurs/utilisateurs");

        LOGGER.info("======================= Fin /gestion/utilisateurs. Méthode formUser");

        return mav;
    }
}
