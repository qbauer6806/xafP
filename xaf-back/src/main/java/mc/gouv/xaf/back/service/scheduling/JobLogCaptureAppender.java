package mc.gouv.xaf.back.service.scheduling;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Appender Logback personnalisé qui capture les logs des jobs Quartz en mémoire.
 * Filtre les logs en fonction de la présence de la clé MDC "demandeJobId".
 */
public class JobLogCaptureAppender extends AppenderBase<ILoggingEvent> {

    public static final String MDC_KEY = "demandeJobId";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    private JobLogCaptureService captureService;

    public void setCaptureService(JobLogCaptureService captureService) {
        this.captureService = captureService;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (captureService == null) {
            return;
        }

        String demandeJobId = event.getMDCPropertyMap().get(MDC_KEY);
        if (demandeJobId == null || !captureService.isCapturing(demandeJobId)) {
            return;
        }

        String timestamp = FORMATTER.format(Instant.ofEpochMilli(event.getTimeStamp()));
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        // Garder uniquement le nom court du logger
        if (loggerName.contains(".")) {
            loggerName = loggerName.substring(loggerName.lastIndexOf('.') + 1);
        }
        String message = event.getFormattedMessage();

        String formattedLog = String.format("[%s] %s %s - %s\n", timestamp, level, loggerName, message);
        captureService.appendLog(demandeJobId, formattedLog);
    }
}
