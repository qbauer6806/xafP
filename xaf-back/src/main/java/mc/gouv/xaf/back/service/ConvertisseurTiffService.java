package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ConvertisseurTiffService {

    Map<String, InputStream> generateTiffs(List<DemandeFileDTO> files) throws IOException;

    Map<String, InputStream> generateTiffs(DemandeFileDTO file) throws IOException;
}
