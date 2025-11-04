package mc.gouv.xaf.back.service.motifs.impl;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.AfTemplateModelProvider;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfMotifsTemplateModelProvider {

    private final Optional<MotifsTemplateModelProvider> motifsTemplateModelProvider;
    private final AfTemplateModelProvider afTemplateModelProvider;

    public Map<String, Object> getModel(DemandeDTO demandeDTO) {
        Map<String, Object> model = afTemplateModelProvider.getGenericModelDemande(demandeDTO);
        motifsTemplateModelProvider.ifPresent(provider -> provider.setModel(model, demandeDTO));
        return model;
    }

}
