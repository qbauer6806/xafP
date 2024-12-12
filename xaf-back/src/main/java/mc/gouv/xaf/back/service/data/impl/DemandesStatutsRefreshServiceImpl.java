package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.service.data.DemandesStatutsRefreshService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesStatutsRefreshServiceImpl implements DemandesStatutsRefreshService {

}
