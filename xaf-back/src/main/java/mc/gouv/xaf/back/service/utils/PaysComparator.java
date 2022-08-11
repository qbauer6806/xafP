package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.servicerest.pays.model.PaysBean;

/**
 * Classe servant à trier des pays par nom
 * 
 * @author qdeme
 *
 */
public class PaysComparator implements Comparator<PaysBean> {
    
    @Override
    public int compare(PaysBean p1, PaysBean p2) {
        return p1.getNom().compareTo(p2.getNom());
    }
    
}
