package mc.gouv.xaf.back.service.motifs.impl;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MotifsTemplateModelProviderImpl extends AbstractMotifsTemplateModelProviderImpl {

    public Map<String, Object> getModel(DemandeDTO demande) {
        return getGenericModel(demande);
    }
}
