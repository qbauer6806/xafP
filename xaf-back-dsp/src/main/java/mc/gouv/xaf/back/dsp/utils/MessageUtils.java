package mc.gouv.xaf.back.dsp.utils;

import mc.gouv.xaf.back.dsp.dto.ResidErrorDTO;

import java.util.List;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String toStringMessage(int httpStatus, String message, List<ResidErrorDTO> errors) {
        StringBuilder builder = new StringBuilder()
                .append("Erreur ")
                .append(httpStatus)
                .append(" - ")
                .append(message)
                .append(":<br>");
        for (ResidErrorDTO erreur : errors) {
            builder.append("  - ")
                    .append(erreur.getClef())
                    .append(" / ")
                    .append(erreur.getNom())
                    .append(" / ")
                    .append(erreur.getLibelle())
                    .append("<br>");
        }
        return builder.toString();
    }

}
