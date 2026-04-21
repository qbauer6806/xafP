package mc.gouv.xaf.backweb.ws;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.UsagersCourrierService;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerDTO;
import mc.gouv.xaf.backweb.dto.AutocompleteUsagerListeDTO;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * WS d'autocomplete pour les usagers courrier
 *
 * @author qdeme
 */
@GouvRestController
@Secured("ROLE_SAISIE")
@RequestMapping(value = "/ws/usagersCourrierAutocomplete", produces = "application/json")
@RequiredArgsConstructor
public class UsagersCourrierAutocompleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCourrierAutocompleteController.class);

    private final UsagersCourrierService usagersCourrierService;

    @GetMapping(value = "/usagers", produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoComplete(@RequestParam String query) {
        LOGGER.info("======================= Appel de /ws/demandesCourrierAutocomplete/usagers");
        AutocompleteUsagerListeDTO ret = usagersAutoComplete(query, false);
        LOGGER.info("======================= Fin appel de /ws/demandesCourrierAutocomplete/usagers");
        return ret;
    }

    @GetMapping(value = "/usagersFullText", produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoCompleteFullText(@RequestParam String query,
            @RequestParam Integer usagerSourceId) {

        LOGGER.info("======================= Appel de /ws/demandesCourrierAutocomplete/usagersFullText");
        AutocompleteUsagerListeDTO ret = usagersAutoComplete(query, true);
        // Supprimer des suggestions l'usager source
        AutocompleteUsagerDTO toRemove = null;
        for (AutocompleteUsagerDTO dcau : ret.getSuggestions()) {
            if (dcau.getData().equals(usagerSourceId.toString())) {
                toRemove = dcau;
            }
        }
        if (toRemove != null) {
            ret.setSuggestions(ArrayUtils.removeElement(ret.getSuggestions(), toRemove));
        }
        LOGGER.info("======================= Fin appel de /ws/demandesCourrierAutocomplete/usagersFullText");
        return ret;
    }

    private AutocompleteUsagerListeDTO usagersAutoComplete(String query, boolean fullText) {

        List<UsagerCourrierDTO> usagers = usagersCourrierService.getUsagersCourrier(query);

        List<AutocompleteUsagerDTO> liste = new ArrayList<>();
        for (UsagerCourrierDTO usager : usagers) {
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

            // Si fullText, afficher aussi l'adresse, le code postal et la ville
            if (fullText) {
                nomRaisonSoc += " - " + usager.getAdresse1() + " " + usager.getCodePostal() + " " + usager.getVille();
            }
            u.setValue(nomRaisonSoc);

            u.setData(usager.getPkUsagersCourrier().toString());
            liste.add(u);
        }
        AutocompleteUsagerListeDTO ret = new AutocompleteUsagerListeDTO();
        ret.setSuggestions(liste.toArray(new AutocompleteUsagerDTO[0]));
        return ret;
    }

}
