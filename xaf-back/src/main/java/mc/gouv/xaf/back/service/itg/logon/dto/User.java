package mc.gouv.xaf.back.service.itg.logon.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable, Principal {

    private Integer id;
    private String matricule;
    private String nom;
    private String mail;
    private String nomUsage;
    private String nomNaissance;
    private String nomAffichage;
    private String prenom;
    private String telPro;
    private String fonction;
    private Set<Role> roles;
    private Civilite civilite;
    private Etat etat;
    private String service;

    @Override
    public String getName() {
        return this.matricule;
    }

    public String getRolesByAppli(String appli) {
        User userWk = this;
        Set<Role> roleSet = userWk.getRoles();
        StringBuilder listRolesMessage = new StringBuilder();

        for (Role r : roleSet) {
            String appliStr = r.getAppli().getCode();
            if (appliStr.trim().compareTo(appli.trim()) == 0) {
                if (listRolesMessage.isEmpty()) {
                    listRolesMessage.append(r.getTitre());
                } else {
                    listRolesMessage.append("; ").append(r.getTitre());
                }
            }
        }

        return listRolesMessage.toString();
    }

}
