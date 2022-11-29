package mc.gouv.xaf.back.paiement;

import org.slf4j.Logger;

public class LoggerMethodeUtils {

    private LoggerMethodeUtils() {
    }

    public static void logStartMethod(Logger logger) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String methodName = stackTrace[1].getMethodName();
        String className = stackTrace[1].getClassName();
        logger.info("START - Call [{}.{}] ", className, methodName);
    }

    public static void logEndMethod(Logger logger) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String methodName = stackTrace[1].getMethodName();
        String className = stackTrace[1].getClassName();
        logger.info("END - Call [{}.{}] ", className, methodName);
    }
}
