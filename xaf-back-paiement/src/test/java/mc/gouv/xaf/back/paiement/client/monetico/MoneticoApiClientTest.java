package mc.gouv.xaf.back.paiement.client.monetico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.itg.monetico.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.monetico.MoneticoApiClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class MoneticoApiClientTest {

    MoneticoApiClient moneticoApiClient = new MoneticoApiClient(new PaiementPropertiesResolverTestImpl(),
            new OperationHelper(new PaiementPropertiesResolverTestImpl()), null, null, null);

    @Disabled
    @Test
    void testCapture() throws Exception {
        CommandeDTO commandeDTO = new CommandeDTO();
        commandeDTO.setMontantInitial(90.0);
        commandeDTO.setMontantRestant(90.0);
        commandeDTO.setMontantDejaCapture(0.0);
        MoyenPaiementDTO moyenPaiementDTO = new MoyenPaiementDTO();
        // TODO moyenPaiementDTO.setPkMoyenPaiements("XQXS2CeBYNrO");
        CommandeOperationDTO operation = new CommandeOperationDTO();
        operation.setMontant(90.0);
        commandeDTO.setMoyenPaiement(moyenPaiementDTO);
        // TODO commandeDTO.setOperations(Collections.singletonList(operation));
        boolean b = moneticoApiClient.capture(commandeDTO, operation, null);
        assertFalse(b); // Pour satisfaire Sonar...
    }

    @Test
    void extractResultTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class,
                CommandeOperationDTO.class);
        method.setAccessible(true);

        String test =
                "version=1.0\n" + "reference=E9M3Xt4glJIX\n" + "cdr=1\n" + "lib=paiement accepte\n" + "aut=949104";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ACCEPTEE.name(), operationDTO.getOperationStatut());
        // TODO assertEquals("949104", operationDTO.getNumeroAutorisation());
        // TODO assertEquals("paiement accepte", operationDTO.getLibelle());
    }

    @Test
    void extractResultAutStringTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class,
                CommandeOperationDTO.class);
        method.setAccessible(true);

        String test =
                "version=1.0\n" + "reference=AETMGSr5yk4k\n" + "cdr=1\n" + "lib=paiement accepte\n" + "aut=SQLZIY";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ACCEPTEE.name(), operationDTO.getOperationStatut());
        // TODO assertEquals("SQLZIY", operationDTO.getNumeroAutorisation());
        // TODO assertEquals("paiement accepte", operationDTO.getLibelle());
    }

    @Test
    void extractResultCaptureRefuseeTest()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class,
                CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" + "reference=000000000145\n" + "cdr=0\n" + "lib=commande non authentifiee";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.REFUSEE.name(), operationDTO.getOperationStatut());
        // TODO assertThat(operationDTO.getNumeroAutorisation()).isNull();
        // TODO assertEquals("commande non authentifiee", operationDTO.getLibelle());
    }

    @Test
    void extractResultErreurTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class,
                CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" + "reference=000000000145\n" + "cdr=-1\n" + "lib=commercant non identifie";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ERREUR.name(), operationDTO.getOperationStatut());
        // TODO assertThat(operationDTO.getNumeroAutorisation()).isNull();
        // TODO assertEquals("commercant non identifie", operationDTO.getLibelle());
    }
}
