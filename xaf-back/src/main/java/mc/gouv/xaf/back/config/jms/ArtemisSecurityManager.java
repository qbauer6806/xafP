package mc.gouv.xaf.back.config.jms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.cert.X509Certificate;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager2;
import org.apache.commons.codec.binary.StringUtils;

/***
 * Voir exemple sur ActiveMQJAASSecurityManager**
 * 
 * @author fgaujous
 *
 */
public class ArtemisSecurityManager implements ActiveMQSecurityManager2 {

    class User {

        private String user;

        private String password;

        // Pour le moment un seul rôle
        private String role;

        User(String user, String password, String role) {
            this.user = user;
            this.password = password;
            this.role = role;
        }

    }

    private String userSender;
    private String passwordSender;
    private String userConsumer;
    private String passwordConsumer;

    public ArtemisSecurityManager() {
    }

    public ArtemisSecurityManager(String userSender, String passwordSender, String userConsumer,
            String passwordConsumer) {

        this.userSender = userSender;
        this.passwordSender = passwordSender;
        this.userConsumer = userConsumer;
        this.passwordConsumer = passwordConsumer;
    }

    // user/password
    private Map<String, User> users;

    @Override
    public boolean validateUser(String user, String password) {
        return validateUserFromMap(user, password);
    }

    @Override
    public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
        throw new UnsupportedOperationException(
                "Invoke validateUserAndRole(String, String, Set<Role>, CheckType, String, RemotingConnection) instead");

    }

    @Override
    public boolean validateUser(String user, String password, X509Certificate[] certificates) {
        return validateUserFromMap(user, password);
    }

    @Override
    public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType,
            String address, RemotingConnection connection) {
        boolean authorized = false;
        // Dans roles il y a tous les roles associé à l'adresse donc sender et consumer
        boolean validUser = validateUserFromMap(user, password);
        if (validUser) {

            Iterator<Role> iterRole = roles.iterator();
            while (!authorized && iterRole.hasNext()) {
                Role r = iterRole.next();
                if (checkType.hasRole(r)) {
                    authorized = true;
                }
            }

        }
        return authorized;
    }

    public void addUser(String user, String password, String role) {
        this.users.put(user, new User(user, password, role));
    }

    private boolean validateUserFromMap(String user, String passwordToCheck) {

        if (users == null) {
            users = new HashMap<>();

            // Ajout des utilisateurs
            addUser(userSender, passwordSender, CustomArtemisSecuritySettingPlugin.ROLE_SENDER);

            addUser(userConsumer, passwordConsumer, CustomArtemisSecuritySettingPlugin.ROLE_CONSUMER);

        }

        if (users.containsKey(user)) {
            User u = users.get(user);
            return StringUtils.equals(passwordToCheck, u.password);
        }
        return false;
    }

}
