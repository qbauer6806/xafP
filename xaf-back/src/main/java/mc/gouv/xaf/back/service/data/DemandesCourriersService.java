package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 *
 * @author qdeme
 */
public interface DemandesCourriersService {

    DemandeCourrierDTO saveCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto);

    DemandeCourrierDTO getCourrier(Integer pkDemande, Integer pkCourrier);

    DemandeCourrierDTO updateCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto);

    Page<DemandeCourrierDTO> getDemandesCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable,
            String[] strings);
}
