package mc.gouv.xaf.back.service.motifs.impl;

import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

    public Map<String, Object> getModel(DemandeDTO demande) {
        return new HashMap<>();
    }
}
