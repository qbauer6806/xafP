package mc.gouv.af.back.motifs.impl;

import mc.gouv.af.back.motifs.MotifsTemplateModelProvider;
import mc.gouv.dem.shared.model.DemandeDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

    public Map<String, Object> getModel(DemandeDTO demande) {
        return new HashMap<>();
    }
}
