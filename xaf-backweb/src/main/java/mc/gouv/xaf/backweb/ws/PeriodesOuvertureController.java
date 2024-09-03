package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@GouvRestController
@RequestMapping("/ws/periodesouverture")
public class PeriodesOuvertureController {

    @Autowired
    private PeriodesOuvertureService periodesOuvertureService;

    @GetMapping
    public Page<PeriodeOuvertureDTO> getPageable(Pageable pageable) {
        return periodesOuvertureService.getPeriodesOuverturePageable(pageable);
    }

}
