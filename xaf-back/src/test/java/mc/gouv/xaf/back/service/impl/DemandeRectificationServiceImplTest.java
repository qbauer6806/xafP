package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.bpm.model.CommentaireInterneDTO;
import mc.gouv.xaf.back.bpm.model.GouvBPMUser;
import mc.gouv.xaf.back.service.AfHistoService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesHistoriqueService;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DemandeRectificationServiceImplTest {

    @Mock
    private DemarchesDataProvider demarchesDataProvider;

    @Mock
    private GouvBPM gouvBPM;

    @Mock
    private AfHistoService histoService;

    @Mock
    private DemandesHistoriqueService demandesHistoriqueService;

    @InjectMocks
    private DemandeRectificationServiceImpl demandeRectificationService;

    private final Integer pkDemande = 123;
    private final String commentaire = "Erreur sur l'adresse";

    @BeforeEach
    void setUp() {
        lenient().when(demarchesDataProvider.getCodeMotifDemandeRectification()).thenReturn("CODE_MOTIF");
        lenient().when(demarchesDataProvider.getStatutEnAttenteRectification()).thenReturn("STATUT_ATTENTE");
        lenient().when(histoService.demanderRectification(anyInt(), any())).thenReturn(new DemandeHistoriqueDTO());
    }

    @Test
    void testDemanderRectification_Success() {
        demandeRectificationService.demanderRectification(pkDemande, commentaire);

        verify(gouvBPM).demanderRectification(eq(pkDemande), any(GouvBPMUser.class),
                eq("CODE_MOTIF"), eq(StringEscapeUtils.escapeHtml4(commentaire)), eq("STATUT_ATTENTE"));
        verify(demandesHistoriqueService).saveHistorique(eq(pkDemande), any(DemandeHistoriqueDTO.class));
        verify(gouvBPM).putCommentaireInterne(eq(pkDemande), any(CommentaireInterneDTO.class));
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

    @Test
    void testDemanderRectification_HistoriqueSaveFails_ShouldLogError() {
        doThrow(new RuntimeException("Erreur lors de la sauvegarde"))
                .when(demandesHistoriqueService).saveHistorique(anyInt(), any(DemandeHistoriqueDTO.class));

        demandeRectificationService.demanderRectification(pkDemande, commentaire);

        verify(gouvBPM).demanderRectification(eq(pkDemande), any(GouvBPMUser.class), eq("CODE_MOTIF"),
                eq(StringEscapeUtils.escapeHtml4(commentaire)), eq("STATUT_ATTENTE"));
        verify(demandesHistoriqueService).saveHistorique(eq(pkDemande), any(DemandeHistoriqueDTO.class));
        verify(gouvBPM).putCommentaireInterne(eq(pkDemande), any(CommentaireInterneDTO.class));
    }
}
