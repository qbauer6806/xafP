package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;

/**
 * Sert à trier l'historique par date
 *
 * @author qdeme
 */
public class DemandeHistoriqueComparator implements Comparator<DemandeHistoriqueDTO> {

    @Override
    public int compare(DemandeHistoriqueDTO d1, DemandeHistoriqueDTO d2) {
        return d1.getDate().compareTo(d2.getDate());
    }

}
