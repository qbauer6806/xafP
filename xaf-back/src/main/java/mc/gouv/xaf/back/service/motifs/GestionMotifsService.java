package mc.gouv.xaf.back.service.motifs;

import java.io.IOException;
import java.util.List;
import mc.gouv.xaf.shared.dto.ExportMotifDTO;

public interface GestionMotifsService {

    String exportConfig() throws IOException;

    List<ExportMotifDTO> importConfig(byte[] file) throws IOException;

}
