package mc.gouv.xaf.back.service.utils;

import java.util.Comparator;

import mc.gouv.file.shared.dto.FileDTO;

public class FileComparator implements Comparator<FileDTO> {

    @Override
    public int compare(FileDTO f1, FileDTO f2) {
        return f1.getName().compareTo(f2.getName());
    }

}
