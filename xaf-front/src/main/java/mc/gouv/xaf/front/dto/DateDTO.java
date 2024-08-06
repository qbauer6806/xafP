package mc.gouv.xaf.front.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DateDTO {

    private String now;

    public DateDTO() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        this.now = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
