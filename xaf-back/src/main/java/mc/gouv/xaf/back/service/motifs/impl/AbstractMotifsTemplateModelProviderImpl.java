package mc.gouv.xaf.back.service.motifs.impl;

import java.util.Map;
import mc.gouv.xaf.back.service.motifs.MotifsTemplateModelProvider;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractMotifsTemplateModelProviderImpl implements MotifsTemplateModelProvider {

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public Map<String, Object> getGenericModel(DemandeDTO demandeDTO) {
        return afBackUtils.getGenericModelMail(demandeDTO);
    }

}
