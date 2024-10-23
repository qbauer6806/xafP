package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.service.DemandeFilesCategorizer;
import mc.gouv.xaf.shared.dto.*;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DemandesFilesCategorizerImplTest {

    private static final String FRONT_FILE = "FRONT_FILE";
    private static final String JUSTIFICATIF_DEMANDE = "JUSTIFICATIF_DEMANDE";
    private static final String FICHIER_INTERNE = "FICHIER_INTERNE";

    private final DemandeFilesCategorizer demandeFilesCategorizer = new AbstractDemandeFilesCategorizerImpl() {

    };

    private DemandeFileDTO createFile(String meta, String name, String url, Date date) {
        DemandeFileDTO file = new DemandeFileDTO();
        file.setMeta(meta);
        file.setName(name);
        file.setUrl(url);
        file.setDate(date);
        return file;
    }

    private DemandeComplementsFileDTO createComplementFile(String meta, String name, String url) {
        DemandeComplementsFileDTO file = new DemandeComplementsFileDTO();
        file.setMeta(meta);
        file.setName(name);
        file.setUrl(url);
        return file;
    }

    private DemandeComplementsDTO createDemandeComplements(DemandeComplementsReponseDTO reponse) {
        DemandeComplementsDTO complement = new DemandeComplementsDTO();
        complement.setReponse(reponse);
        return complement;
    }

    private DemandeComplementsReponseDTO createReponse(String meta, String name, String url, Date date) {
        DemandeComplementsReponseDTO reponse = new DemandeComplementsReponseDTO();
        reponse.setDate(date);
        reponse.setFichiers(new DemandeComplementsFileDTO[] { createComplementFile(meta, name, url) });
        return reponse;
    }

    @Test
    void getCategoriesAndFilesTest() {
        DemandeDTO demande = new DemandeDTO();

        DemandeFileDTO[] files = new DemandeFileDTO[4];
        // Fichiers de demande initiale (meta vide ou meta commence par "FRONT_"
        files[0] = createFile(null, "vide", "front/vide", new Date());
        files[1] = createFile(FRONT_FILE, "file", "front/file", new Date());
        // Fichiers remis par l'Administration
        files[2] = createFile(JUSTIFICATIF_DEMANDE, "justif", "justif/file", new Date());
        // Fichiers internes
        files[3] = createFile(FICHIER_INTERNE, "interne", "interne/file", new Date());

        // Compléments de la demande, l'un des deux a une réponse
        DemandeComplementsDTO[] complements = new DemandeComplementsDTO[2];
        complements[0] = createDemandeComplements(null);
        complements[1] = createDemandeComplements(createReponse(null, "complement", "complement", new Date()));

        demande.setFichiers(files);
        demande.setComplements(complements);

        List<FileCategoryDTO> result = demandeFilesCategorizer.getCategoriesAndFiles(demande);

        assertEquals(4, result.size());
        assertEquals("Fichiers de la demande initiale", result.getFirst().getName());
        List<DemandeFileDTO> fileList = result.getFirst().getFiles();
        assertEquals(2, fileList.size());
        assertEquals("vide", fileList.get(0).getName());
        assertEquals("file", fileList.get(1).getName());
        assertEquals("Fichiers complémentaires", result.get(1).getName());
        fileList = result.get(1).getFiles();
        assertEquals(1, fileList.size());
        assertEquals("complement", fileList.getFirst().getName());
        assertEquals("Fichiers remis par l'Administration", result.get(2).getName());
        fileList = result.get(2).getFiles();
        assertEquals(1, fileList.size());
        assertEquals("justif", fileList.getFirst().getName());
        assertEquals("Fichiers internes", result.get(3).getName());
        fileList = result.get(3).getFiles();
        assertEquals(1, fileList.size());
        assertEquals("interne", fileList.getFirst().getName());
    }

    @Test
    void getCategoriesAndFilesTestPasDeFichiers() {
        DemandeDTO demande = new DemandeDTO();
        List<FileCategoryDTO> result = demandeFilesCategorizer.getCategoriesAndFiles(demande);

        assertEquals(4, result.size());
        assertEquals("Fichiers de la demande initiale", result.getFirst().getName());
        List<DemandeFileDTO> fileList = result.get(0).getFiles();
        assertEquals(0, fileList.size());
        assertEquals("Fichiers complémentaires", result.get(1).getName());
        fileList = result.get(1).getFiles();
        assertEquals(0, fileList.size());
        assertEquals("Fichiers remis par l'Administration", result.get(2).getName());
        fileList = result.get(2).getFiles();
        assertEquals(0, fileList.size());
        assertEquals("Fichiers internes", result.get(3).getName());
        fileList = result.get(3).getFiles();
        assertEquals(0, fileList.size());
    }

}
