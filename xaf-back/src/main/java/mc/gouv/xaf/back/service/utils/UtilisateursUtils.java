package mc.gouv.xaf.back.service.utils;

import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.Civilite;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour la gestion des utilisateurs.
 * <br>
 * Les utilisateurs sont les agents qui s'occupent des demandes dans le Back-Office.
 * <br>
 * Leurs données viennent de l'application LOGON (mc.gouv.logon)
 *
 * @author mboutelier.ext
 */
@Component
public class UtilisateursUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateursUtils.class);

    @Autowired
    @Lazy
    private UtilisateursCache utilisateursCache;

    /**
     * Retourne le prénom et le nom d'un utilisateur à partir de son matricule.
     * <br>
     * Retourne {@code null} si le maticule donné n'est pas trouvé dans logon.
     *
     * @param matricule, String contenant le matricule de l'usager
     * @return Une String contenant le prénom et le nom d'un utilisateur.
     */
    public String getUserNameFromID(String matricule) {
        LOGGER.debug("getUserNameFromID() : Appel à Logon afin de récupérer l'utilisateur {}...", matricule);
        if (StringUtils.isNotBlank(matricule)) {
            User user = utilisateursCache.get(matricule);
            if (user != null) {
                String value = AfBackUtils.escapeChars(user.getPrenom() + " " + user.getNomAffichage());
                return StringEscapeUtils.escapeHtml4(value);
            }
        }
        return null;
    }

    public String getUserFullNameFromUser(User user) {
        StringBuilder builder = new StringBuilder();
        if (user != null) {
            Civilite civilite = user.getCivilite();
            if (civilite != null) {
                CiviliteUtilisateurs civ = CiviliteUtilisateurs.valueOf(civilite.toString());
                builder.append(civ.getAbbreviation()).append(' ');
            }
            String prenom = user.getPrenom();
            if (prenom != null) {
                builder.append(AfBackUtils.escapeChars(prenom)).append(' ');
            }
            String nom = StringUtils.isNotEmpty(user.getNomUsage()) ? user.getNomUsage() : user.getNomNaissance();
            if (nom != null) {
                builder.append(AfBackUtils.escapeChars(nom));
            }
        }
        return StringEscapeUtils.escapeHtml4(builder.toString());
    }

}
