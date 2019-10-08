#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import mc.gouv.af.back.motifs.MotifsTemplateModelProvider;
import mc.gouv.dem.shared.model.DemandeDTO;
import ${groupId}.shared.model.v1568884433537.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Primary
@Component
public class ${artifactIdCamelCase}MotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

    @Override
    public Map<String, Object> getModel(DemandeDTO demande) {
        Map<String, Object> motifsModel = new HashMap<>();

        ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);
        motifsModel.put("joursFeries", ${artifactIdCamelCase}Utils.convertJourFeriesTypesToSentence(contenuDemande));
        motifsModel.put("joursFeriesList", ${artifactIdCamelCase}Utils.convertJourFeriesTypesToList(contenuDemande));
        motifsModel.put("joursFeriesExceptionnels", ${artifactIdCamelCase}Utils.convertJourFeriesExceptionnelsToSentence(contenuDemande));
        motifsModel.put("joursFeriesExceptionnelsList", ${artifactIdCamelCase}Utils.convertJourFeriesExceptionnelsToList(contenuDemande));

        return motifsModel;
    }
}
