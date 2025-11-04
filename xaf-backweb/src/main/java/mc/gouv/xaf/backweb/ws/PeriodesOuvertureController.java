package mc.gouv.xaf.backweb.ws;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@GouvRestController
@Secured({ "ROLE_PARAMETRAGE", "ROLE_CONFIGURATION" })
@RequestMapping("/ws/periodesouverture")
@RequiredArgsConstructor
public class PeriodesOuvertureController {

    private final PeriodesOuvertureService periodesOuvertureService;

    @GetMapping
    public Page<PeriodeOuvertureDTO> getPageable(Pageable pageable) {
        return periodesOuvertureService.getPeriodesOuverturePageable(pageable);
    }

}
