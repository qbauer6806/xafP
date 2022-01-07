package mc.gouv.xaf.back.service.utils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import mc.gouv.xaf.shared.dto.TypedocDTO;

/**
 * Sert à trier l'historique par date
 * 
 * @author qdeme
 *
 */
public class TypedocComparator implements Comparator<TypedocDTO> {
	
	private static Collator collator = Collator.getInstance(Locale.FRENCH);
	
	static {
		collator.setStrength(Collator.PRIMARY);
	}
    
    @Override
    public int compare(TypedocDTO t1, TypedocDTO t2) {
        return collator.compare(t1.getValue(), t2.getValue());
    }
    
}
