package mc.gouv.xaf.api.date.util;

import java.text.FieldPosition;
import java.util.Date;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * https://stackoverflow.com/questions/45276807/iso8601-with-milliseconds-in-json-using-jackson
 *
 * @author fgaujous
 */
public class ISO8601WithMillisDateFormat extends ISO8601DateFormat {

    /**
     *
     */
    private static final long serialVersionUID = 1623437518082844753L;

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        String value = ISO8601Utils.format(date, true); // "true" to include milliseconds
        toAppendTo.append(value);
        return toAppendTo;
    }
}
