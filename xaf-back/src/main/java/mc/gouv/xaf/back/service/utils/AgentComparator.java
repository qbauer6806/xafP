package mc.gouv.xaf.back.service.utils;

import mc.gouv.logon.shared.User;

import java.util.Comparator;

public class AgentComparator implements Comparator<User> {

    @Override
    public int compare(User u1, User u2) {
        String u1Prenom = "";
        if (u1.getPrenom() != null) {
            u1Prenom = u1.getPrenom();
        }
        String u2Prenom = "";
        if (u2.getPrenom() != null) {
            u2Prenom = u2.getPrenom();
        }
        return u1Prenom.compareTo(u2Prenom);
    }

}
