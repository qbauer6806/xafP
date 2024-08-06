package mc.gouv.xaf.back.service.itg.logon;

import java.util.List;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "logonClient", url = "${mc.gouv.logon.api.url:}")
public interface LogonClient {

    @GetMapping(value = "/user/logged/{sessionId}", produces = "application/json")
    User getLoggedUser(@PathVariable("sessionId") String sessionId);

    @GetMapping(value = "/user/appli/{codeAppli}", produces = "application/json")
    List<User> getListUserByCodeAppli(@PathVariable("codeAppli") String codeAppli);

    @GetMapping(value = "/user/mat/{matricule}", produces = "application/json")
    User getUserByMatricule(@PathVariable("matricule") String matricule);

}
