package mc.gouv.xaf.backweb.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerDTO;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerListeDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

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

    @GetMapping(value = "/usagers", produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoComplete(@RequestParam String query) {

        LOGGER.info("======================= Appel de /ws/usagersAutocomplete/usagers");

        Collection<GichuniUsagerDTO> usagers = usagersCache.getAll().values();
        Set<AutocompleteUsagerDTO> liste = new TreeSet<>();
        for (GichuniUsagerDTO usager : usagers) {
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

        List<AutocompleteUsagerDTO> toDelete = new ArrayList<>();
        for (AutocompleteUsagerDTO dcau : liste) {
            if (!Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE).matcher(dcau.getValue()).find()) {
                toDelete.add(dcau);
            }
        }
        toDelete.forEach(liste::remove);

        for (AutocompleteUsagerDTO dcau : liste) {
            if (Integer.parseInt(dcau.getData()) >= DemarchesUtils.USAGERID_OFFSET) {
                dcau.setValue(dcau.getValue() + " (courrier)");
            }
        }

        AutocompleteUsagerListeDTO ret = new AutocompleteUsagerListeDTO();

        ret.setSuggestions(liste.toArray(new AutocompleteUsagerDTO[0]));

        LOGGER.info("======================= Fin appel de /ws/usagersAutocomplete/usagers");

        return ret;

    }

}
