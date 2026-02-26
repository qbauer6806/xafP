package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.client.FoyerClient;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home-member")
@RequiredArgsConstructor
public class FoyerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoyerController.class);

    private final XafFrontserverUtils xafUtils;
    private final FoyerClient foyerClient;

    @GetMapping
    public ResponseEntity<Object> get(HttpServletRequest req) {
        UsagerInfosDTO user = xafUtils.getLoggedUser(req);
        if (user == null) {
            return xafUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }
        return foyerClient.get(user.getTokenInfo().getAccessToken());
    }

    @PostMapping
    public ResponseEntity<Object> post(@RequestBody Object body, HttpServletRequest req) {
        UsagerInfosDTO user = xafUtils.getLoggedUser(req);
        if (user == null) {
            return xafUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }
        return foyerClient.post(user.getTokenInfo().getAccessToken(), body);
    }
}
