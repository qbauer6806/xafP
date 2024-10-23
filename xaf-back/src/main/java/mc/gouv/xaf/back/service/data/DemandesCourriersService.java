package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 *
 * @author qdeme
 */
public interface DemandesCourriersService {

    DemandeCourrierDTO saveCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto);

    DemandeCourrierDTO getCourrier(Integer pkDemande, Integer pkCourrier);

    List<DemandeCourrierDTO> getCourriers(Integer pkDemande);

    List<DemandeCourrierDTO> getCourriers();

    /**
     * Supprime tous les courriers de la demande donnée en paramètre (dans le BO est dans file)
     *
     * @param pkDemande
     *         : ID de la demande dont il faut supprimer les courriers
     */
    void deleteCourriers(Integer pkDemande);

    DemandeCourrierDTO updateCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto);

    Page<DemandeCourrierDTO> getDemandesCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable,
            String[] strings);
}
