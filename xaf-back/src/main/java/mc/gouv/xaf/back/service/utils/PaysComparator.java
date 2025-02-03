package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.xaf.shared.dto.PaysDTO;

/**
 * Classe servant à trier des pays par nom
 *
 * @author qdeme
 */
public class PaysComparator implements Comparator<PaysDTO> {

    @Override
    public int compare(PaysDTO p1, PaysDTO p2) {
        return p1.getLibelle().compareTo(p2.getLibelle());
    }

}
