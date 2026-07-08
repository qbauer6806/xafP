package mc.gouv.xaf.back.service.scheduling;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de capture des logs en mémoire pour les jobs Quartz.
 * Stocke les messages de log par identifiant de job (demandeJobId).
 */
@Component
public class JobLogCaptureService {

    public static final int MAX_LOG_LENGTH = 50000;

    private final ConcurrentHashMap<String, StringBuilder> logMap = new ConcurrentHashMap<>();

    /**
     * Démarre la capture des logs pour un job donné.
     */
    public void startCapture(String demandeJobId) {
        logMap.put(demandeJobId, new StringBuilder());
    }

    /**
     * Ajoute un message de log pour un job donné.
     */
    public void appendLog(String demandeJobId, String message) {
        StringBuilder sb = logMap.get(demandeJobId);
        if (sb != null && sb.length() < MAX_LOG_LENGTH) {
            sb.append(message);
        }
    }

    /**
     * Arrête la capture et retourne les logs capturés pour un job donné.
     */
    public String stopCapture(String demandeJobId) {
        StringBuilder sb = logMap.remove(demandeJobId);
        if (sb == null) {
            return "";
        }
        String result = sb.toString();
        if (result.length() > MAX_LOG_LENGTH) {
            return result.substring(0, MAX_LOG_LENGTH) + "\n... (logs tronqués)";
        }
        return result;
    }

    /**
     * Vérifie si une capture est en cours pour un job donné.
     */
    public boolean isCapturing(String demandeJobId) {
        return logMap.containsKey(demandeJobId);
    }
}
