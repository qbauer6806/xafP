package mc.gouv.xaf.back.paiement.service;

import mc.gouv.file.shared.dto.FileDTO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ConvertisseurTiffService {

    List<FileDTO> generateTiffs(List<FileDTO> files) throws IOException;

    FileDTO generateTiff(FileDTO file) throws IOException;
}
