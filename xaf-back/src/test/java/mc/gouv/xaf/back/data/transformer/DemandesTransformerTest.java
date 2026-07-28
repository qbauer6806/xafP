package mc.gouv.xaf.back.data.transformer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesComplementsBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.entity.DemandesDataBO;
import mc.gouv.xaf.back.data.entity.DemandesFilesBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeStatutDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandesTransformerTest {

    static Method getAllFields;
    static final String FIELD_COURRIER = "courriers";
    static final String FIELD_FILES = "files";
    static final String FIELD_STATUS = "statuts";
    static final String FIELD_DEM_COMPL = "demandesComplements";
    static final String FIELD_DATA = "data";

    @InjectMocks
    private DemandesTransformer demandesTransformer;

    @Mock
    private DemandesUsagersTransformer demandesUsagersTransformer;

    @Mock
    private DemandesAgentsTransformer demandesAgentsTransformer;

    @Mock
    private DemarchesDataProvider demarchesDataProvider;

    @BeforeEach
    void beforeEach() throws NoSuchMethodException {
        getAllFields = DemandesTransformer.class.getDeclaredMethod("getAllFields", String[].class);
        getAllFields.setAccessible(true);
    }

    public static Stream<Arguments> getAllFieldsTestArgs() {
        return Stream.of(Arguments.of(null, new boolean[] { true, true, true, true, true }, new String[] {},
                new boolean[] { false, false, false, false, false }, new String[] { FIELD_COURRIER },
                new boolean[] { true, false, false, false, false }, new String[] { FIELD_COURRIER, FIELD_DATA },
                new boolean[] { true, false, false, false, true },
                new String[] { FIELD_COURRIER, FIELD_DATA, FIELD_FILES },
                new boolean[] { true, true, false, false, true },
                new String[] { FIELD_COURRIER, FIELD_DATA, FIELD_FILES, FIELD_STATUS },
                new boolean[] { true, true, true, false, true },
                new String[] { FIELD_COURRIER, FIELD_DATA, FIELD_FILES, FIELD_DEM_COMPL },
                new boolean[] { true, true, true, true, true }));
    }

    @ParameterizedTest
    @MethodSource("getAllFieldsTestArgs")
    void getAllFieldsTest(String[] fields, boolean[] expected)
            throws InvocationTargetException, IllegalAccessException {
        boolean[] result = (boolean[]) getAllFields.invoke(DemandesTransformer.class, (Object) fields);
        assertArrayEquals(expected, result);
    }

    @Test
    void bo2DtoMapDernierStatut() {
        String[] fields = new String[] {};
        DemandeBO demandeBO = makeDemandeBo();
        demandeBO.setDernierStatut(null);

        // Si on a pas de statut dans le BO on ne mappe pas dans le DTO
        DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBO, fields);
        assertNull(demandeDTO.getDernierStatut());

        try (MockedStatic<DemarchesUtils> demarchesUtils = mockStatic(DemarchesUtils.class);
                MockedStatic<DemandesStatutsTransformer> statutsTransformer = mockStatic(
                        DemandesStatutsTransformer.class)) {
            DemandeStatutDTO statutDTO = new DemandeStatutDTO();
            statutDTO.setAgentId("agentId");

            demarchesUtils.when(DemarchesUtils::isFrontUser).thenReturn(false);
            statutsTransformer.when(() -> DemandesStatutsTransformer.bo2Dto((DemandesStatutsBO) any()))
                    .thenReturn(statutDTO);

            // Si on a un statut, on le mappe...
            demandeBO.setDernierStatut(new DemandesStatutsBO());
            demandeDTO = demandesTransformer.bo2Dto(demandeBO, fields);

            assertNotNull(demandeDTO.getDernierStatut());
            assertEquals("agentId", statutDTO.getAgentId());

            // Si on est un utilisateur front on cache l'agentId
            when(DemarchesUtils.isFrontUser()).thenReturn(true);

            demandeBO.setDernierStatut(new DemandesStatutsBO());
            demandeDTO = demandesTransformer.bo2Dto(demandeBO, fields);

            assertNotNull(demandeDTO.getDernierStatut());
        }

    }

    @Test
    void bo2DtoBooleanFieldsTest() {
        String[] fields = new String[] { FIELD_COURRIER, FIELD_DATA, FIELD_FILES, FIELD_DEM_COMPL };
        DemandeBO demandeBO = makeDemandeBo();

        try (MockedStatic<DemandesCourriersTransformer> courriersTransformer = mockStatic(
                DemandesCourriersTransformer.class);
                MockedStatic<DemandesComplementsTransformer> complementsTransformer = mockStatic(
                        DemandesComplementsTransformer.class);
                MockedStatic<DemandesFilesTransformer> filesTransformer = mockStatic(DemandesFilesTransformer.class);
                MockedStatic<DemandesDataTransformer> dataTransformer = mockStatic(DemandesDataTransformer.class)) {

            DemandeCourrierDTO demandeCourrierDTO = mock(DemandeCourrierDTO.class);
            DemandeFileDTO demandeFileDTO = mock(DemandeFileDTO.class);
            DemandeDataDTO demandeDataDTO = mock(DemandeDataDTO.class);
            DemandeComplementsDTO complementsDTO = mock(DemandeComplementsDTO.class);

            courriersTransformer.when(() -> DemandesCourriersTransformer.bo2Dto((DemandesCourriersBO) any()))
                    .thenReturn(demandeCourrierDTO);
            complementsTransformer.when(() -> DemandesComplementsTransformer.bo2Dto((DemandesComplementsBO) any()))
                    .thenReturn(complementsDTO);
            filesTransformer.when(() -> DemandesFilesTransformer.bo2Dto((DemandesFilesBO) any()))
                    .thenReturn(demandeFileDTO);
            dataTransformer.when(() -> DemandesDataTransformer.bo2Dto((DemandesDataBO) any()))
                    .thenReturn(demandeDataDTO);

            DemandeDTO demandeDTO = demandesTransformer.bo2Dto(demandeBO, fields);

            assertNotNull(demandeDTO.getCourriers());
            assertNotNull(demandeDTO.getData());
            assertNotNull(demandeDTO.getFichiers());
            assertNotNull(demandeDTO.getComplements());
        }
    }

    private DemandeBO makeDemandeBo() {
        ObjectMapper mapper = new ObjectMapper();
        DemandeBO demandeBO = new DemandeBO();
        try {
            demandeBO.setContenu(mapper.readTree(
                    "{\"donnee\":{\"demandeur\":{\"titre\":null,\"prenom\":\"Tom\",\"nom\":\"TORREZE\",\"email\":null},\"derogation\":{\"typedemande\":\"SUSPENSION\",\"annee\":\"2019\",\"dateinfosal\":null,\"effectifentreprise\":\"45\",\"presencedeleguepersonnel\":\"NO\",\"datederniereelection\":null,\"dateinfodp\":null,\"identitedp\":null,\"motifdemande\":\"cds\"},\"attribut\":{\"demandeur\":{\"declarant\":\"DECLARANT\"},\"civilite\":null,\"monegasque\":null,\"adresse\":{\"ligne1\":null,\"ligne2\":null,\"ligne3\":null,\"codePostal\":null,\"ville\":null,\"pays\":null},\"email\":\"ttorreze.ext@gouv.mc\",\"date\":{\"heure\":{\"naissance\":null}},\"lieu\":{\"naissance\":null},\"telephone\":{\"indicatif\":null,\"numero\":null},\"fiscale\":{\"titulaire\":null,\"bic\":null,\"iban\":null},\"declarant\":{\"civilite\":\"0\",\"nom\":\"Tomconsult\",\"prenom\":\"a,b,c\",\"monegasque\":\"NO\",\"resident\":\"NO\",\"adresse\":{\"ligne1\":\"2, rue du pioupiou\",\"ligne2\":null,\"ligne3\":null,\"codePostal\":\"Monaco\",\"ville\":\"98000\",\"pays\":\"FR\"}},\"nom\":null,\"prenoms\":null,\"resident\":null}},\"raison\":{\"sociale\":\"\"}}"));
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
        demandeBO.setFkAccess(new AccessBO());
        demandeBO.setCourriers(Set.of(new DemandesCourriersBO()));
        demandeBO.setFiles(Set.of(new DemandesFilesBO()));
        demandeBO.setDemandesComplements(Set.of(new DemandesComplementsBO()));
        demandeBO.setData(Set.of(new DemandesDataBO()));
        demandeBO.setCanal(DemandeCanalEnum.GUICHET_PHYSIQUE.name());

        return demandeBO;
    }

    @Test
    void bo2DtoNullTest() {
        DemandeDTO demande = demandesTransformer.bo2Dto((DemandeBO) null);
        assertNull(demande);

        demande = demandesTransformer.bo2Dto((DemandeBO) null, null);
        assertNull(demande);

        demande = demandesTransformer.bo2Dto((DemandeBO) null, new String[] { FIELD_FILES });
        assertNull(demande);
    }
}
