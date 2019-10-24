package mc.gouv.xaf.back.service.utils;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.servicerest.usager.model.UsagerBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.logon.shared.User.Civilite;

/**
 * Classe utilitaire pour la gestion des utilisateurs.
 * <br>
 * Les utilisateurs sont les agents qui s'occupent des demandes dans le Back-Office.
 * <br>
 * Leurs données viennent de l'application LOGON (mc.gouv.logon)
 * 
 * @author mboutelier.ext
 *
 */
@Component
public class UtilisateursUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateursUtils.class);

	@Autowired
	private UtilisateursCache utilisateursCache;

	@Autowired
	private UsagersCache usagersCache;

	/**
	 * Retourne le prénom et le nom d'un utilisateur à partir de son matricule.
	 * <br>
	 * Retourne {@code null} si le maticule donné n'est pas trouvé dans logon.
	 * 
	 * @param userId
	 * @return
	 * @throws RestException
	 */
	public String getUserNameFromID(String matricule) throws RestException {
		LOGGER.debug("getUserNameFromID() : Appel à Logon afin de récupérer l'utilisateur {}...", matricule);
		User user = utilisateursCache.get(matricule);
		if (user != null) {
			return user.getPrenom() + " " + user.getNomAffichage();
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
				builder.append(prenom).append(' ');
			}
			String nom = StringUtils.isNotEmpty(user.getNomUsage()) ? user.getNomUsage() : user.getNomNaissance();
			if (nom != null) {
				builder.append(nom);
			}
		}
		return builder.toString();
	}

	public String getUsagerCourrierFromId(Integer usagerId) {
		LOGGER.debug("getUsagerFromId() : Récupération de l'usager courrier {}...", usagerId);
		UsagerBean usagerCourrier = usagersCache.get(usagerId);
		String nomUsager = "";
		if (usagerCourrier != null) {
			if (!StringUtils.isEmpty(usagerCourrier.getNom())) {
				nomUsager = StringUtils.defaultString(usagerCourrier.getPrenom()) + " " + usagerCourrier.getNom();
			} else {
				nomUsager = usagerCourrier.getRaisonSociale();
			}
		}
		return StringUtils.trim(StringUtils.defaultString(nomUsager));
	}

}
