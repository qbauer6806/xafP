package mc.gouv.xaf.back.controller;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import mc.gouv.file.shared.dto.FileResponseDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.RecapDemandesDTO;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.dto.v1.UsagerDemandesRecapDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

/**
 * Interface spécifiant les méthodes devant être implémentées dans l'API dite "2/3" Ce sont donc des méthodes visant à
 * être appelées par le Back Office tiers (non GENTS) via le FO GENTS (pour des raisons de ségmentation réseau)
 *
 * @author qdeme
 */
public interface AfApiController2Tiers {

    /*
     *
     * Stockage config en DB (motifs et périodes d'ouverture)
     *
     */

    List<MotifDTO> getMotifs();

    MotifDTO createMotif(MotifDTO motif);

    MotifDTO updateMotif(MotifDTO motif);

    void deleteMotif(Integer pkMotif);

    List<PeriodeOuvertureDTO> getPeriodesOuverture();

    PeriodeOuvertureDTO createPeriodeOuverture(PeriodeOuvertureDTO periodeOuverture);

    PeriodeOuvertureDTO updatePeriodeOuverture(PeriodeOuvertureDTO periodeOuverture);

    void deletePeriodeOuverture(Integer pkPeriodeOuverture);

    /*
     *
     * Proxy vers API GICHUNI pour récupérer les informations de profil usager
     *
     */

    GichuniUsagerDTO getUsager(Integer usagerId);

    /*
     *
     * Proxy vers FILE pour le stockage de fichiers
     *
     */

    // ATTENTION : GERER LE FAIT QUE LE CLIENT DOIT GENERER LE HASH ! Les TS doivent générer le HASH ! Le faire à la place du client tiers ?
    // TODO Etudier ça

    FileResponseDTO saveFile(String container, MultipartFile data, HttpServletRequest request,
            HttpServletResponse response);

    ResponseEntity<InputStreamResource> getFile(String container, HttpServletRequest request,
            HttpServletResponse response);

    ResponseEntity deleteFile(@PathVariable("container") String container, HttpServletRequest request);

    /*
     *
     * Pour communication avec MonGuichet.mc (asynchrone via Kafka (BUSMSG))
     *
     */

    ResponseEntity notifyCreationDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateCreation, RecapDemandesDTO recapDemandes);

    ResponseEntity notifyChangementStatutDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes);

    ResponseEntity notifySuppressionDemande(Integer usagerId, Integer demandeId, String identifiantDemande,
            Date dateSuppression, RecapDemandesDTO recapDemandes);

    ResponseEntity notifyDesinscriptionUsagerTS(Integer usagerId);

    ResponseEntity synchronizeDemandesRecaps(List<UsagerDemandesRecapDTO> usagerDemandesRecap);

    ResponseEntity notifyCreationAccesTS(Integer usagerId);

}
