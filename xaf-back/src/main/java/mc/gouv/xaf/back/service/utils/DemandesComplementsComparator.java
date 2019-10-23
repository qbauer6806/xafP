package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.xaf.back.shared.dto.DemandeComplementsDTO;

/**
 * Sert à trier les demandes d'informations complémentaires par date
 * 
 * @author qdeme
 *
 */
public class DemandesComplementsComparator implements Comparator<DemandeComplementsDTO> {
    
    @Override
    public int compare(DemandeComplementsDTO d1, DemandeComplementsDTO d2) {
        return d1.getQuestion().getDate().compareTo(d2.getQuestion().getDate());
    }
    
}
