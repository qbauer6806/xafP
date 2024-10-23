package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.xaf.shared.dto.DemandeStatutDTO;

/**
 * Sert à trier les statuts par date
 *
 * @author qdeme
 */
public class DemandeStatutComparator implements Comparator<DemandeStatutDTO> {

    @Override
    public int compare(DemandeStatutDTO d1, DemandeStatutDTO d2) {
        return d1.getDate().compareTo(d2.getDate());
    }

}
