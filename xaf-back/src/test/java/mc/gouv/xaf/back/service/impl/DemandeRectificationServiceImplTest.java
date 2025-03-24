package mc.gouv.xaf.back.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.back.service.histo.DemandesHistoriqueService;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandeRectificationServiceImplTest {

    @Mock
    private DemarchesDataProvider demarchesDataProvider;
    @Mock
    private GouvBPM gouvBPM;
    @Mock
    private DemandesHistoriqueService demandesHistoriqueService;
    @Mock
    private DemandesCommentaireService demandesCommentaireService;
    @InjectMocks
    private DemandeRectificationServiceImpl demandeRectificationService;

    private final Integer pkDemande = 123;
    private final String commentaire = "Erreur sur l'adresse";

    @BeforeEach
    void setUp() {
        lenient().when(demarchesDataProvider.getCodeMotifDemandeRectification()).thenReturn("CODE_MOTIF");
        lenient().when(demarchesDataProvider.getStatutEnAttenteRectification()).thenReturn("STATUT_ATTENTE");
        lenient().when(demandesHistoriqueService.statusChangeAgent(any())).thenReturn(new DemandeHistoriqueDTO());
    }

    @Test
    void testDemanderRectification_Success() {
        demandeRectificationService.demanderRectification(pkDemande, commentaire);

        verify(gouvBPM).demanderRectification(eq(pkDemande), any(GouvBPMUser.class),
                eq("CODE_MOTIF"), eq(StringEscapeUtils.escapeHtml4(commentaire)), eq("STATUT_ATTENTE"));
        verify(demandesHistoriqueService).saveHisto(eq(pkDemande), any(DemandeHistoriqueDTO.class));
        verify(demandesCommentaireService).putCommentaireInterne(any(DemandeCommentaireDTO.class));
    }

    @Test
    void testDemanderRectification_MissingCodeMotif_ShouldThrowException() {
        when(demarchesDataProvider.getCodeMotifDemandeRectification()).thenReturn(null);

        DemarcheException thrown = assertThrows(DemarcheException.class,
                () -> demandeRectificationService.demanderRectification(pkDemande, commentaire));
        assertEquals("Le code motif de demande de rectification n'est pas définit", thrown.getMessage());
    }

    @Test
    void testDemanderRectification_MissingStatut_ShouldThrowException() {
        when(demarchesDataProvider.getStatutEnAttenteRectification()).thenReturn(null);

        DemarcheException thrown = assertThrows(DemarcheException.class,
                () -> demandeRectificationService.demanderRectification(pkDemande, commentaire));
        assertEquals("Le statut de la demande de rectification n'est pas définit", thrown.getMessage());
    }

    @Test
    void testDemanderRectification_EmptyComment_ShouldThrowException() {
        DemarcheException thrown = assertThrows(DemarcheException.class,
                () -> demandeRectificationService.demanderRectification(pkDemande, ""));
        assertEquals("Impossible d'insérer un commentaire vide", thrown.getMessage());
    }

}
