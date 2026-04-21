package mc.gouv.xaf.backweb.ws;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.backweb.controller.AbstractController;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@GouvRestController
@Secured("ROLE_SAISIE")
@RequestMapping("/ws/courriers")
@RequiredArgsConstructor
public class RechercheCourriersController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RechercheCourriersController.class);

    private final DemandesCourriersService demandesCourriersService;

    @GetMapping(value = "/pageable")
    public Page<DemandeCourrierDTO> getDemandesCourriers(@RequestParam(value = "texte", required = false) String texte,
            @RequestParam(value = "imprime", required = false) boolean imprime, Pageable pageable) {

        String safeTexte = AfBackUtils.logSafe(texte);
        LOGGER.info("======================= Appel de /ws/courriers/pageable (texte=\"{}\")", safeTexte);

        DemandeCourrierRechercheDTO demandeRecherche = new DemandeCourrierRechercheDTO();
        demandeRecherche.setTexte(texte);
        demandeRecherche.setImprime(imprime);

        if (!pageable.getSort().isUnsorted()) {
            Order order = pageable.getSort().iterator().next();
            if (order != null) {
                return demandesCourriersService.getDemandesCourriers(demandeRecherche, pageable, new String[] {});
            }
        }

        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.ASC,
                "identifiant");

        LOGGER.info("======================= Fin appel de /ws/demandes/pageable");

        return demandesCourriersService.getDemandesCourriers(demandeRecherche, newPageable, new String[] {});
    }

}
