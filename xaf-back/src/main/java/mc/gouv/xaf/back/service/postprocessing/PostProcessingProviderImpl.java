package mc.gouv.xaf.back.service.postprocessing;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class PostProcessingProviderImpl implements PostProcessingProvider {

}
