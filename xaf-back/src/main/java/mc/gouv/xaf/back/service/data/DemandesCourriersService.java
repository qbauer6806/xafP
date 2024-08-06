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

    DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto);

    DemandeCourrierDTO getCourrier(String demarcheId, Integer pkDemande, Integer pkCourrier);

    List<DemandeCourrierDTO> getCourriers(String demarcheId, Integer pkDemande);

    List<DemandeCourrierDTO> getCourriersPourDemarche(String demarcheId);

    /**
     * Supprime tous les courriers de la demande donnée en paramètre (dans le BO est dans file)
     *
     * @param demarcheId : ID de la démarche concernée
     * @param pkDemande  : ID de la demande dont il faut supprimer les courriers
     */
    void deleteCourriers(String demarcheId, Integer pkDemande);

    DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto);

    Page<DemandeCourrierDTO> getDemandesCourriers(DemandeCourrierRechercheDTO demandeRecherche, Pageable pageable, String[] strings);
}
