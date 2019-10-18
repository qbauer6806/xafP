package mc.gouv.xaf.backweb.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.xaf.back.cache.UsagersCache;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerDTO;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerListeDTO;
import mc.gouv.dem.service.util.DemarchesUtils;
import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

/**
 * 
 * WS d'autocomplete pour les usagers
 * 
 * @author qdeme
 * 
 */
@GouvRestController
@RequestMapping(value = "/ws/usagersAutocomplete", produces = "application/json")
public class UsagersAutocompleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersAutocompleteController.class);

    @Autowired
    private UsagersCache usagersCache;

    @RequestMapping(value = "/usagers", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoComplete(
            @RequestParam(required = true) String query) throws Exception {

        LOGGER.info("======================= Appel de /ws/usagersAutocomplete/usagers");

        Collection<UsagerBean> usagers = usagersCache.getAll().values();
        Set<AutocompleteUsagerDTO> liste = new TreeSet<AutocompleteUsagerDTO>();
        for (UsagerBean usager : usagers) {
            AutocompleteUsagerDTO u = new AutocompleteUsagerDTO();

            // Nom + raison sociale si indiquée lors de la création
            String nomRaisonSoc = usager.getPrenom();
            if (StringUtils.isBlank(nomRaisonSoc)) {
                nomRaisonSoc = usager.getNom();
            } else {
                nomRaisonSoc += " " + usager.getNom();
            }

            if (StringUtils.isNotBlank(usager.getRaisonSociale())) {
                nomRaisonSoc = nomRaisonSoc + " (" + usager.getRaisonSociale() + ")";
            }
            u.setValue(nomRaisonSoc);

            u.setData(usager.getId().toString());
            liste.add(u);
        }

        List<AutocompleteUsagerDTO> toDelete = new ArrayList<AutocompleteUsagerDTO>();
        for (AutocompleteUsagerDTO dcau : liste) {
            if (!Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(dcau.getValue()).find()) {
                toDelete.add(dcau);
            }
        }
        liste.removeAll(toDelete);

        for (AutocompleteUsagerDTO dcau : liste) {
            if (Integer.parseInt(dcau.getData()) >= DemarchesUtils.USAGERID_OFFSET) {
                dcau.setValue(dcau.getValue() + " (courrier)");
            }
        }

        AutocompleteUsagerListeDTO ret = new AutocompleteUsagerListeDTO();

        ret.setSuggestions(liste.toArray(new AutocompleteUsagerDTO[liste.size()]));

        LOGGER.info("======================= Fin appel de /ws/usagersAutocomplete/usagers");

        return ret;

    }

}
