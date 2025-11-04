package mc.gouv.xaf.back.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesFilesRepository;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.UploadPieceJustificativeService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.UploadFileDTO;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class UploadPieceJustificativeServiceImpl implements UploadPieceJustificativeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPieceJustificativeServiceImpl.class);

    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final FileService fileService;
    private final DemandesService demandesService;
    private final DemandesFilesService demandesFilesService;
    private final DemandesFilesRepository demandesFilesRepository;
    private final UtilisateursUtils utilisateursUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> enregistrerPieceJustificative(Integer pkDemande, MultipartFile[] files,
            List<UploadFileDTO> metadonnees, HttpServletResponse response) {
        DemandeDTO demande = demandesService.getDemande(pkDemande);
        List<String> fichiersNonEnregistres = new ArrayList<>();
        Arrays.stream(files).forEach(file -> {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename) && metadonnees.stream()
                    .anyMatch(f -> originalFilename.equals(f.getNom()))) {
                LOGGER.info("Part à traiter : {}", AfBackUtils.logSafe(originalFilename));
                LOGGER.info("Appel au FileService...");
                try {
                    // construction d'un nouveau MultipartFile avec le nom modifié
                    String safeFileName = FileNameUtils.getSafeFileName(originalFilename);
                    MultipartFile multipartFile = new RenamedMultipartFile(file, safeFileName);
                    String urlFile = fileService.saveFile(demande, gouvPropertiesResolver.getContainerId(),
                            multipartFile, response);
                    UploadFileDTO uploadFileDTO = metadonnees.stream().filter(f -> originalFilename.equals(f.getNom()))
                            .findFirst().orElse(new UploadFileDTO());

                    DemandeFileDTO demandeFileDTO = new DemandeFileDTO();
                    demandeFileDTO.setName(safeFileName);
                    demandeFileDTO.setTypedoc(uploadFileDTO.getType());
                    demandeFileDTO.setUrl(URLDecoder.decode(urlFile, UTF_8));
                    String contextVisibilite = uploadFileDTO.isVisibilite()
                            ? FileUtils.META_BACK_FRONT
                            : FileUtils.META_BACK;
                    String meta = contextVisibilite + "COMPLEMENT_DEMANDE;" + FileUtils.generateMetaData(file)
                            + this.addAgentName();
                    demandeFileDTO.setMeta(meta);
                    demandeFileDTO.setDate(new Date());

                    demandesFilesService.saveFile(demandeFileDTO, pkDemande);
                } catch (IOException e) {
                    LOGGER.error("Erreur lors de l'enregistrement de la pièce justificative dans file {}",
                            originalFilename, e);
                    fichiersNonEnregistres.add(originalFilename);
                }
            } else if (StringUtils.isNotBlank(originalFilename)) {
                fichiersNonEnregistres.add(originalFilename);
            }
        });

        if (CollectionUtils.isNotEmpty(fichiersNonEnregistres)) {
            String body = String.format("Les fichiers suivants n'ont pas été enregistrés %s",
                    String.join(",", fichiersNonEnregistres));
            return ResponseEntity.status(500).body(body);
        }
        return ResponseEntity.ok().body("Le(s) fichier(s) ont été enregistré(s) avec succès ");
    }

    private String addAgentName() {
        String agentId = AfBackUtils.getAuthenticatedAgentId();
        String agentName = utilisateursUtils.getUserNameFromID(agentId);
        return String.format(";AGENT_NAME_%s", agentName);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public ResponseEntity<String> supprimerPieceJustificative(Integer idDemandeFile) {
        Optional<DemandesFilesBO> demandesFilesBO = demandesFilesRepository.findById(idDemandeFile);
        if (demandesFilesBO.isEmpty()) {
            return ResponseEntity.status(500)
                    .body(String.format("La pièce justificative avec l'identifiant %s n'existe pas", idDemandeFile));
        }
        DemandesFilesBO demandeFile = demandesFilesBO.get();
        String url = demandeFile.getUrl();
        if (demandesFilesRepository.countByUrl(url)
                == 1) { // On supprime le fichier s'il n'est référencé qu'une seule fois
            LOGGER.info("Le fichier {} sera effacé de file.", url);

            String urlEncode = URLEncoder.encode(url, UTF_8);
            if (urlEncode != null && urlEncode.startsWith("/")) {
                urlEncode = urlEncode.substring(1);
            }

            fileService.deleteFile(gouvPropertiesResolver.getContainerId(), urlEncode);
            demandesFilesRepository.delete(demandeFile);
            return ResponseEntity.ok().body("Le fichier a été supprimé avec succès");
        } else {
            return ResponseEntity.status(500)
                    .body(String.format("Impossible de supprimer le fichier %s car référencé ailleurs", url));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<String> changerVisibiliteFichier(Integer idDemandeFile, boolean visibleUsager) {
        DemandesFilesBO demandesFilesBO = demandesFilesRepository.findById(idDemandeFile)
                .orElseThrow(() -> new IllegalStateException("demande file non trouvée pour l'id: " + idDemandeFile));
        String updatedMeta;
        if (visibleUsager) {
            updatedMeta = StringUtils.replace(demandesFilesBO.getMeta(), FileUtils.META_BACK,
                    FileUtils.META_BACK_FRONT);
        } else {
            updatedMeta = StringUtils.replace(demandesFilesBO.getMeta(), FileUtils.META_BACK_FRONT,
                    FileUtils.META_BACK);
        }
        demandesFilesBO.setMeta(updatedMeta);
        demandesFilesRepository.save(demandesFilesBO);
        return ResponseEntity.ok().body("Le fichier a été mis à jour avec succès");
    }

    private record RenamedMultipartFile(MultipartFile delegate, String renamedFilename) implements MultipartFile {

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getOriginalFilename() {
            return renamedFilename;
        }

        @Override
        public String getContentType() {
            return delegate.getContentType();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public long getSize() {
            return delegate.getSize();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return delegate.getBytes();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            delegate.transferTo(dest);
        }
    }

}
