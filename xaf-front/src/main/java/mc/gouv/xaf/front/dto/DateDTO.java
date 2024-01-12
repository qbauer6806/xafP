package mc.gouv.candifp.frontserver.movetoxaf.dto;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateDTO {

    private String now;

    public DateDTO() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        this.now = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }
}
