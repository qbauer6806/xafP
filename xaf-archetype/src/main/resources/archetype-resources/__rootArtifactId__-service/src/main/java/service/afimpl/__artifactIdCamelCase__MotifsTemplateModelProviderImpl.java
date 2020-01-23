#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import java.util.HashMap;
import java.util.Map;

import ${groupId}.shared.model.v1573825612706.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Primary
@Component
public class ${artifactIdCamelCase}MotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

	@Override
	public Map<String, Object> getModel(DemandeDTO demande) {
		Map<String, Object> motifsModel = new HashMap<>();
		ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);
		if (contenuDemande != null) {
			motifsModel.put("salariesADetacher", ${artifactIdCamelCase}Utils.getSalariesADetacher(contenuDemande));
		}
		motifsModel.put("nomChantier", contenuDemande.getDonnee().getEntreprise().getNomchantier());
		motifsModel.put("nomEntreprise", contenuDemande.getDonnee().getEntreprise().getNomentreprise());
		motifsModel.put("identifiant", demande.getIdentifiant());
		return motifsModel;
	}
}
