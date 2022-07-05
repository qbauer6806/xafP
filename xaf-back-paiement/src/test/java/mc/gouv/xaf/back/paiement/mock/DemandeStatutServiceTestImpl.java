package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.paiement.service.DemandeStatutService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DemandeStatutServiceTestImpl implements DemandeStatutService {
    @Override
    public String getEnAttenteDeTraitement() {
        return DemandeStatutEnum.EN_ATTENTE_TRAIT.name();
    }
}
