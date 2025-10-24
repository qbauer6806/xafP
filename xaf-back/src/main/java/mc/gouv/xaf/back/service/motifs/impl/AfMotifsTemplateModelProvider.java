package mc.gouv.xaf.back.service.motifs.impl;

import java.util.Map;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AfMotifsTemplateModelProvider extends AfTemplateModelProvider {

    @Autowired(required = false)
    private MotifsTemplateModelProvider motifsTemplateModelProvider;

    public Map<String, Object> getModel(DemandeDTO demandeDTO) {
        Map<String, Object> model = getGenericModelMail(demandeDTO);
        if (motifsTemplateModelProvider != null) {
            motifsTemplateModelProvider.setModel(model, demandeDTO);
        }
        return model;
    }

}
