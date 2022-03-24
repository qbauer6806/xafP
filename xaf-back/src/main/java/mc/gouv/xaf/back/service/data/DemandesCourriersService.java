package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 * 
 * @author qdeme
 *
 */
public interface DemandesCourriersService {

    public DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto);

    public DemandeCourrierDTO getCourrier(String demarcheId, Integer pkDemande, Integer pkCourrier);

    public List<DemandeCourrierDTO> getCourriers(String demarcheId, Integer pkDemande);

    public List<DemandeCourrierDTO> getCourriersPourDemarche(String demarcheId);
    
    /**
     * Supprime tous les courriers de la demande donnée en paramètre (dans le BO est dans file)
     * 
     * @param demarcheId : ID de la démarche concernée
     * @param pkDemande : ID de la demande dont il faut supprimer les courriers 
     */
    public void deleteCourriers(String demarcheId, Integer pkDemande);

    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto);

}
