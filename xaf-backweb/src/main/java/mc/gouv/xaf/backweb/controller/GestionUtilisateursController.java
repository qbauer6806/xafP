package mc.gouv.xaf.backweb.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import mc.gouv.Static;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.logon.apiclient.LogonApiClient;
import mc.gouv.logon.shared.User;

/**
 * Controller pour les fonctionnalites (onglets) Utilisateurs et Parametres
 * 
 * @author tverdoyan
 * 
 */
@Controller
@Secured("ROLE_PARAMETRAGE")
@RequestMapping("/gestion/utilisateurs")
public class GestionUtilisateursController extends AbstractController {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionUtilisateursController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView formUser(Model model) {

        LOGGER.info("Appel de la page /gestion/utilisateurs. Méthode formUser");
        List<User> list = null;

        try {
            LogonApiClient logonApiClient = new LogonApiClient(Static.getValue(LogonApiClient.DEFAULT_GOUV_PROPERTY_URL));
            list = logonApiClient.getRessUser().getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
            model.addAttribute("userList", list);
        } catch (Exception e) {
            LOGGER.error("Exception rencontrée dans formUser. Msg : " + e);
        }

        ModelAndView mav = new ModelAndView("gestion/utilisateurs/utilisateurs");

        LOGGER.info("======================= Fin /gestion/utilisateurs. Méthode formUser");

        return mav;
    }
}
