package mc.gouv.af.backweb.ws;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.backweb.dto.AutocompleteUsagerDTO;
import mc.gouv.af.backweb.dto.AutocompleteUsagerListeDTO;
import mc.gouv.dem.service.UsagersCourrierService;
import mc.gouv.dem.shared.model.UsagerCourrierDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

/**
 * 
 * WS d'autocomplete pour les usagers courrier
 * 
 * @author qdeme
 * 
 */
@GouvRestController
@RequestMapping(value = "/ws/usagersCourrierAutocomplete", produces = "application/json")
public class UsagersCourrierAutocompleteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsagersCourrierAutocompleteController.class);

    @Autowired
    private UsagersCourrierService usagersCourrierService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @RequestMapping(value = "/usagers", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoComplete(
            @RequestParam(required = true) String query) throws Exception {

        LOGGER.info("======================= Appel de /ws/demandesCourrierAutocomplete/usagers");

        AutocompleteUsagerListeDTO ret = usagersAutoComplete(query, false);

        LOGGER.info("======================= Fin appel de /ws/demandesCourrierAutocomplete/usagers");

        return ret;

    }
    
    @RequestMapping(value = "/usagersFullText", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody AutocompleteUsagerListeDTO usagersAutoCompleteFullText(
            @RequestParam(required = true) String query,
            @RequestParam(required = true) Integer usagerSourceId) throws Exception {

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

        List<UsagerCourrierDTO> usagers = usagersCourrierService.getUsagersCourrier(gouvPropertiesResolver.getDemarcheId(), query);
        
        List<AutocompleteUsagerDTO> liste = new ArrayList<AutocompleteUsagerDTO>();
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
        ret.setSuggestions(liste.toArray(new AutocompleteUsagerDTO[liste.size()]));
        
        return ret;
    }

}
