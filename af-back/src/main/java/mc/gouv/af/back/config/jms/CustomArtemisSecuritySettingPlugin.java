package mc.gouv.af.back.config.jms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.SecuritySettingPlugin;
import org.apache.activemq.artemis.core.settings.HierarchicalRepository;

/***
 * Exemple*:http://programtalk.com/vs/?source=activemq-artemis/artemis-server/src/main/java/org/apache/activemq/artemis/core/server/impl/LegacyLDAPSecuritySettingPlugin.java
 **
 * 
 * @author fgaujous
 *
 */
public class CustomArtemisSecuritySettingPlugin implements SecuritySettingPlugin {

    public static final String PERMISSION_CREATE_NON_DURABLE_QUEUE = "createNonDurableQueue";
    public static final String PERMISSION_DELETE_NON_DURABLE_QUEUE = "deleteNonDurableQueue";
    public static final String PERMISSION_CREATE_DURABLE_QUEUE = "createDurableQueue";
    public static final String PERMISSION_DELETE_DURABLE_QUEUE = "deleteDurableQueue";
    public static final String PERMISSION_CONSUME = "consume";
    public static final String PERMISSION_BROWSE = "browse";
    public static final String PERMISSION_SEND = "send";

    public static final String ROLE_SENDER = "sender";
    public static final String ROLE_CONSUMER = "consumer";

    private Map<String, Set<Role>> securityRoles;
    private HierarchicalRepository<Set<Role>> securityRepository;
    private String queueName;

    public CustomArtemisSecuritySettingPlugin(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public SecuritySettingPlugin init(Map<String, String> options) {
        return this;
    }

    @Override
    public SecuritySettingPlugin stop() {

        return this;

    }

    @Override
    public Map<String, Set<Role>> getSecurityRoles() {
        if (securityRoles == null) {
            securityRoles = new HashMap<String, Set<Role>>();
            HashSet<Role> roles = new HashSet<Role>();

            // Role sender pour l'envoi des messages
            Role roleDem = new Role(ROLE_SENDER, true, false, true, false, false, false, false);
            roles.add(roleDem);

            // Role consumer
            Role roleHab = new Role(ROLE_CONSUMER, false, true, true, false, false, false, false);
            roles.add(roleHab);
            securityRoles.put(queueName, roles);

        }
        return securityRoles;
    }

    @Override
    public void setSecurityRepository(HierarchicalRepository<Set<Role>> securityRepository) {
        this.securityRepository = securityRepository;

    }

}
