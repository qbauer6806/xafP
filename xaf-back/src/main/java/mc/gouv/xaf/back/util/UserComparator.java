package mc.gouv.xaf.back.util;

import java.util.Comparator;

import mc.gouv.logon.shared.User;

/**
 * Classe servant à trier des agents par nom
 * 
 * @author qdeme
 *
 */
public class UserComparator implements Comparator<User> {
    
    @Override
    public int compare(User u1, User u2) {
        return u1.getNom().compareTo(u2.getNom());
    }
    
}
