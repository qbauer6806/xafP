package mc.gouv.xaf.back.xls;

import mc.gouv.xaf.back.DemandeContenuMockDTO;
import mc.gouv.xaf.shared.dto.DemandeAgentDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;

public class ExcelMockObjects {

    public static DemandeFlatDTO getDemandeFlatDtoMock() {

        DemandeFlatDTO flatDto = new DemandeFlatDTO();

        flatDto.setAgentAffecteId(ExcelMockData.AGENT_AFFECT_ID);
        flatDto.setAgentAffecteNom(ExcelMockData.AGENT_AFFECT_NAME);
        flatDto.setCanal(ExcelMockData.CANAL);
        flatDto.setCourrierDateReception(ExcelMockData.COURRIER_DATE_RECEPTION);
        flatDto.setCourrierRefInterne(ExcelMockData.COURRIER_REF_INTERNE);
        flatDto.setDateCreation(ExcelMockData.DATE_CREATION);
        flatDto.setDernierStatut(ExcelMockData.DERNIER_STATUT);
        flatDto.setIdentifiant(ExcelMockData.IDENTIFIANT);
        flatDto.setUsagerEmail(ExcelMockData.USAGER_EMAIL);
        flatDto.setUsagerNom(ExcelMockData.USAGER_NOM);
        flatDto.setUsagerPrenom(ExcelMockData.USAGER_PRENOM);
        flatDto.setObservations(ExcelMockData.OBSERVATIONS);
        flatDto.setLangue(ExcelMockData.LANGUE);

        return flatDto;
    }

    public static DemandeDTO getDemandDtoMock() {

        DemandeDTO dto = new DemandeDTO();

        dto.setAgent(new DemandeAgentDTO(ExcelMockData.AGENT_AFFECT_ID));
        dto.setLangue(ExcelMockData.LANGUE);
        DemandeUsagerDTO usager = new DemandeUsagerDTO();
        usager.setEmail(ExcelMockData.USAGER_EMAIL);
        usager.setNom(ExcelMockData.USAGER_NOM);
        usager.setPrenom(ExcelMockData.USAGER_PRENOM);
        dto.setUsager(usager);
        dto.setDateDerModif(ExcelMockData.COURRIER_DATE_DERNIER_MODIFICATION);

        return dto;
    }

    public static DemandeContenuMockDTO getDemandeContenuMockDTO() {
        DemandeContenuMockDTO dto = new DemandeContenuMockDTO();

        dto.setAddressLigne1(ExcelMockData.ADDRESS_LIGNE1);
        dto.setAddressLigne2(ExcelMockData.ADDRESS_LIGNE2);
        dto.setCodePostal(ExcelMockData.CODE_POSTAL);
        dto.setVille(ExcelMockData.VILLE);
        dto.setCommentaire(ExcelMockData.COMMENT);
        dto.setCyclo(true);
        dto.setPermisA(true);
        dto.setPermisB(true);
        dto.setDiplome(ExcelMockData.DIPLOME);
        dto.setPays(ExcelMockData.COUNTRY);
        dto.setInfoSuppl(ExcelMockData.INFO_SUPPL);
        dto.setCompetence(ExcelMockData.SKILLS);

        return dto;
    }

    public static DemandeExcelFlatMockDTO getDemandeExcelFlatMockDTO() {
        DemandeExcelFlatMockDTO dto = new DemandeExcelFlatMockDTO(getDemandeFlatDtoMock(), getDemandeContenuMockDTO());
        dto.setAssermentations("Direction des Affaires Maritimes");
        dto.setLangues(ExcelMockData.LANGUEUAGES);
        dto.setFonctionRecherchee(ExcelMockData.VACANCY);
        dto.setUsagerTitre(ExcelMockData.USAGER_TITRE);

        return dto;
    }

}



