package mc.gouv.xaf.back.service.utils;

import mc.gouv.xaf.back.service.itg.file.service.dto.FileDTO;
import java.util.Comparator;

public class FileComparator implements Comparator<FileDTO> {

    @Override
    public int compare(FileDTO f1, FileDTO f2) {
        return f1.getName().compareTo(f2.getName());
    }

}
