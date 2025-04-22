package mc.gouv.xaf.back.dsp.service.itg.resid.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mc.gouv.xaf.back.dsp.dto.ResidCaisseOuverteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidInformationDebitDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;

import mc.gouv.xaf.back.dsp.dto.ResidAdresseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeCertificatResidenceCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeChangementSituationCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeDuplicataCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeNouvelleCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeRenouvellementCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidEnfantDTO;
import mc.gouv.xaf.back.dsp.dto.ResidEtatsDemandesUpdatedAfterDTO;
import mc.gouv.xaf.back.dsp.dto.ResidHttpResponseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidIdTSDTO;
import mc.gouv.xaf.back.dsp.dto.ResidLoyerPeriodiciteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidMoyensExistenceDTO;
import mc.gouv.xaf.back.dsp.dto.ResidQualiteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidResidentCorrespondanceDTO;
import mc.gouv.xaf.back.dsp.dto.ResidStatutDemandeDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidContactDNL1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidEnfantsDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidIdentiteDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidInitialDemandeParamDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidMembresFoyerDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidNationalite1Et2DTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidPersonneDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidResidenceDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidResidentDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidSituationFamilialeDLN1FDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.back.dsp.enums.common.ResidCiviliteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidLoyerPeriodiciteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidQualiteEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSexeEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidSituationFamilialeEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypeCarteMroadEnum;
import mc.gouv.xaf.back.dsp.enums.common.ResidTypePieceIdentiteEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationDLN1FEnum;
import mc.gouv.xaf.back.dsp.enums.v2.ResidRelationEnum;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidApiService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

@Component
@Primary
@Profile("residmock")
public class ResidApiServiceMockImpl implements ResidApiService {

    @Override
    public ResidUsagerNpdhlDTO getUsagerDln1f(ResidInitialDemandeParamDTO param, String url, String jwt,
            Integer usagerId) throws RestClientException {
        ResidUsagerNpdhlDTO usagerMock = new ResidUsagerNpdhlDTO();
        // Set identite
        ResidIdentiteDLN1FDTO identite = new ResidIdentiteDLN1FDTO();
        identite.setTitreUsager(ResidCiviliteEnum.MONSIEUR);
        identite.setNomUsager("MyNomUsager");
        identite.setNomUsageUsager("MyNomUsageUsager");
        identite.setPrenomUsager("MyPrenomUsager");
        identite.setDateNaissanceUsager("1901-01-01");
        identite.setHeureNaissanceUsager("15:00");
        identite.setVilleNaissanceUsager("MyVilleNaissance");
        identite.setPaysNaissanceUsager("FRA");
        identite.setSexeUsager(ResidSexeEnum.HOM);
        identite.setPersonnaliteSensible(true);

        // Set contacts
        ResidContactDNL1FDTO contacts = new ResidContactDNL1FDTO();
        contacts.setEmail("MyEmail@yopmail.com");
        contacts.setTelephone1Prefix("+33");
        contacts.setTelephone1("0606060606");
        contacts.setEmail("test@email.com");
        contacts.setTypeCommunication("MyTypeCommunication");
        contacts.setLangue("FRA");

        // Set residence
        ResidResidenceDLN1FDTO residence = new ResidResidenceDLN1FDTO();
        ResidQualiteDTO qualite = new ResidQualiteDTO();
        qualite.setQualiteEnum(ResidQualiteEnum.PROPRIETAIRE);
        qualite.setAutre("AutreQualité");
        residence.setQualite(qualite);
        residence.setLocationLogement(false);
        residence.setNombreOccupant(2);
        residence.setLoyer(1000);
        residence.setNombrePiece(3);
        residence.setNombreStationnement(2);
        residence.setSurfaceM2(55);
        ResidLoyerPeriodiciteDTO loyerPeriodicite = new ResidLoyerPeriodiciteDTO();
        loyerPeriodicite.setLoyerEnum(ResidLoyerPeriodiciteEnum.MENSUEL);
        loyerPeriodicite.setAutre("MyLoyerPeriodiciteAutre");
        residence.setLoyerPeriodicite(loyerPeriodicite);

        // Set moyenExistence
        ResidMoyensExistenceDTO moyenExistence = new ResidMoyensExistenceDTO();
        moyenExistence.setSituationPrincipale(ResidSituationEnum.DEMANDEURDEMANDEUSE_D_EMPLOI);
        moyenExistence.setEmployeurRaisonSociale("MyEmployeurRaisonSociale");
        moyenExistence.setEmployeurVille("MyEmployeurVille");
        moyenExistence.setEmployeurPays("FRA");

        // Set adresse
        ResidAdresseDTO adresse = new ResidAdresseDTO();
        adresse.setCareOf("MyCareOf");
        adresse.setAdresse1("MyAdresse1");
        adresse.setAdresse2("MyAdresse2");
        adresse.setAdresse3("MyAdresse3");
        adresse.setAdresse4("MyAdresse4");
        adresse.setPaysCode("FRA");
        adresse.setBloc("MyBloc");
        adresse.setEtage("5");
        adresse.setAppartement("3B26");

        // Set nationalite
        ResidNationalite1Et2DTO nationalite = new ResidNationalite1Et2DTO();
        nationalite.setNationalite1("FRA");
        nationalite.setTypePiece(ResidTypePieceIdentiteEnum.PAS);
        nationalite.setNumeroPiece("789546");
        nationalite.setDateDelivrance("2020-01-01");
        nationalite.setDateFinValidite("2025-01-01");
        nationalite.setPaysDelivrance("FRA");
        nationalite.setNationalite2("PRT");
        nationalite.setRessortissant(false);

        // Set resident
        ResidResidentDLN1FDTO resident = new ResidResidentDLN1FDTO();
        resident.setNumeroCarte("45465");
        resident.setDateDebutValidite("2020-01-01");
        resident.setDateFinValidite("2025-01-01");
        resident.setType(ResidTypeCarteMroadEnum.ORD);
        resident.setDateEtablissementMonaco("2020-02-02");

        // Set situationFamiliale
        ResidSituationFamilialeDLN1FDTO situationFamiliale = new ResidSituationFamilialeDLN1FDTO();
        situationFamiliale.setSituationFamiliale(ResidSituationFamilialeEnum.MAR);
        situationFamiliale.setTitre(ResidCiviliteEnum.MONSIEUR);
        situationFamiliale.setNom("MyNom");
        situationFamiliale.setPrenom("MyPrenom");
        situationFamiliale.setDateNaissance("2001-01-01");
        situationFamiliale.setNationalite("FRA");
        situationFamiliale.setRelation(ResidRelationEnum.CDV);
        situationFamiliale.setFoyer(true);
        situationFamiliale.setLieuNomEntreprise("Test lieu nom entreprise");

        // Set enfants
        ResidEnfantsDLN1FDTO enfants = new ResidEnfantsDLN1FDTO();
        ResidEnfantDTO enfant = new ResidEnfantDTO();
        enfant.setTitreEnfant(ResidCiviliteEnum.MADEMOISELLE);
        enfant.setNomEnfant("MyEnfantNom");
        enfant.setPrenomEnfant("MyEnfantPrenom");
        enfant.setDateNaissanceEnfant("2015-01-01");
        enfant.setNationaliteEnfant("FRA");
        enfant.setRelationEnfant(ResidRelationEnum.ENF);
        enfant.setFoyerEnfant(true);
        enfant.setAutoriteParentaleEnfant(true);
        enfant.setSexeEnfant(ResidSexeEnum.FEM);
        enfant.setAdresseEnfant(adresse);
        enfant.setLieuScolariteTravail("MyLieuScolarite");
        List<ResidEnfantDTO> enfantList = new ArrayList<>();
        enfantList.add(enfant);
        enfants.setEnfants(enfantList);
        enfants.setHasEnfantMineur(true);
        enfants.setNombreEnfantsMineur(1);

        // Set membreFoyer 1
        ResidMembresFoyerDLN1FDTO membresFoyer = new ResidMembresFoyerDLN1FDTO();
        ResidPersonneDLN1FDTO personne = new ResidPersonneDLN1FDTO();
        personne.setTitrePersonne(ResidCiviliteEnum.MONSIEUR);
        personne.setNomPersonne("MyPersonneNom");
        personne.setPrenomPersonne("MyPersonnePrenom");
        personne.setDateNaissancePersonne("1991-01-15");
        personne.setNationalitePersonne("PRT");
        personne.setRelationPersonne(ResidRelationDLN1FEnum.COU);
        personne.setLieuScolariteTravail("Lieu scolarité travail personne");

        // Set membreFoyer 2
        ResidPersonneDLN1FDTO personne2 = new ResidPersonneDLN1FDTO();
        personne2.setTitrePersonne(ResidCiviliteEnum.MADAME);
        personne2.setNomPersonne("MyPersonneNom2");
        personne2.setPrenomPersonne("MyPersonnePrenom2");
        personne2.setDateNaissancePersonne("1990-01-15");
        personne2.setNationalitePersonne("PRT");
        personne2.setRelationPersonne(ResidRelationDLN1FEnum.FRA);
        personne2.setLieuScolariteTravail("Lieu scolarité travail personne 2");

        List<ResidPersonneDLN1FDTO> personneList = new ArrayList<>();
        personneList.add(personne);
        personneList.add(personne2);
        membresFoyer.setHasOtherPersonne(true);
        membresFoyer.setPersonne(personneList);

        usagerMock.setIdentite(identite);
        usagerMock.setContacts(contacts);
        usagerMock.setResidence(residence);
        usagerMock.setMoyenExistence(moyenExistence);
        usagerMock.setAdresse(adresse);
        usagerMock.setNationalite(nationalite);
        usagerMock.setResident(resident);
        usagerMock.setSituationFamiliale(situationFamiliale);
        usagerMock.setEnfants(enfants);
        usagerMock.setMembresFoyer(membresFoyer);
        return usagerMock;
    }

    @Override
    public ResidCaisseOuverteDTO getCaisseOuverte(String url, String jwt) {
        ResidCaisseOuverteDTO caisseOuverteDTO = new ResidCaisseOuverteDTO();
        caisseOuverteDTO.setOpen(true);
        return caisseOuverteDTO;
    }

    @Override
    public ResidHttpResponseDTO submitRetourDebit(ResidInformationDebitDTO informationDebit, String url, String jwt)
            throws JsonProcessingException {
        return null;
    }

    @Override
    public ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {
        return null;
    }

    @Override
    public ResidHttpResponseDTO submitRenouvellementCarteResid(
            ResidDemandeRenouvellementCarteCompleteDTO carteRenouvellement, Map<Integer, DemandeFileDTO> files,
            String url, String jwt) throws IOException {
        return null;
    }

    @Override
    public ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {
        return null;
    }

    @Override
    public ResidHttpResponseDTO submitChangementSituationResid(
            ResidDemandeChangementSituationCompleteDTO changementsituation, Map<Integer, DemandeFileDTO> files,
            String url, String jwt) throws IOException {
        return null;
    }

    @Override
    public ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {
        return null;
    }

    @Override
    public ResidStatutDemandeDTO getEtatDemande(ResidIdTSDTO idDemande, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException {
        return null;
    }

    @Override
    public List<ResidStatutDemandeDTO> getEtatMultipleDemandes(List<ResidIdTSDTO> idsDemandes, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException {
        return new ArrayList<>();
    }


    @Override
    public List<ResidResidentCorrespondanceDTO> getListResidCorrespondance(String numeroCarte, String url, String jwt) {
        return new ArrayList<>();
    }

}
