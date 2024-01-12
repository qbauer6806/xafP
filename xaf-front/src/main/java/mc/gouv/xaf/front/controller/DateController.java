package mc.gouv.xaf.front.controller;

import mc.gouv.xaf.front.dto.DateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/date")
public class DateController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateController.class);

    @GetMapping
    public ResponseEntity doGet() {
        LOGGER.info("====================== /date doGet()");
        return ResponseEntity.ok(new DateDTO());
    }
}
