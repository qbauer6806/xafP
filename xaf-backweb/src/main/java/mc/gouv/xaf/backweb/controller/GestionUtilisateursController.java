package mc.gouv.xaf.backweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.CacheService;
import mc.gouv.xaf.back.service.itg.logon.LogonClient;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.shared.dto.CacheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class GestionUtilisateursController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GestionUtilisateursController.class);

    private final CacheService cacheService;
    private final LogonClient logonClient;
    private final GouvPropertiesResolver gouvPropertiesResolver;

    @GetMapping
    public ModelAndView formUser(Model model) {

        LOGGER.info("Appel de la page /gestion/utilisateurs. Méthode formUser");

        ObjectMapper mapper = new ObjectMapper();

        LOGGER.info("Appel de l'API LOGON...");
        List<User> users = logonClient.getListUserByCodeAppli(gouvPropertiesResolver.getDemarcheId());
        CacheDTO logonUsersCache = new CacheDTO();
        logonUsersCache.setPkCache("LOGON_USERS");
        logonUsersCache.setData(mapper.valueToTree(users));
        cacheService.updateCache(logonUsersCache);
        model.addAttribute("userList", users);
        ModelAndView mav = new ModelAndView("gestion/utilisateurs/utilisateurs");

        LOGGER.info("======================= Fin /gestion/utilisateurs. Méthode formUser");

        return mav;
    }
}
