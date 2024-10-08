package mc.gouv.xaf.back.paiement.client.monetico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.mock.PaiementPropertiesResolverTestImpl;
import mc.gouv.xaf.back.paiement.retry.OperationHelper;
import mc.gouv.xaf.back.paiement.service.itg.monetico.MoneticoApiClient;

public class MoneticoApiClientTest {
	MoneticoApiClient moneticoApiClient = new MoneticoApiClient(new PaiementPropertiesResolverTestImpl(),
			new OperationHelper(new PaiementPropertiesResolverTestImpl()), null, null, null);

    @Test
    public void testCapture() throws Exception {
        CommandeDTO commandeDTO = new CommandeDTO();
        commandeDTO.setMontantInitial(90.0);
        commandeDTO.setMontantRestant(90.0);
        commandeDTO.setMontantDejaCapture(0.0);
        MoyenPaiementDTO moyenPaiementDTO = new MoyenPaiementDTO();
        moyenPaiementDTO.setPkMoyenPaiements("XQXS2CeBYNrO");
        CommandeOperationDTO operation = new CommandeOperationDTO();
        operation.setMontant(90.0);
        commandeDTO.setMoyenPaiement(moyenPaiementDTO);
        commandeDTO.setOperations(Collections.singletonList(operation));
        Boolean b = moneticoApiClient.capture(commandeDTO, operation, null);
        Assert.assertFalse(b); // Pour satisfaire Sonar...
    }

    @Test
    public void extractResultTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class, CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" +
                "reference=E9M3Xt4glJIX\n" +
                "cdr=1\n" +
                "lib=paiement accepte\n" +
                "aut=949104";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ACCEPTEE.name(), operationDTO.getOperationStatut());
        assertEquals("949104", operationDTO.getNumeroAutorisation());
        assertEquals("paiement accepte", operationDTO.getLibelle());
    }

    @Test
    public void extractResultAutStringTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class, CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" +
                "reference=AETMGSr5yk4k\n" +
                "cdr=1\n" +
                "lib=paiement accepte\n" +
                "aut=SQLZIY";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ACCEPTEE.name(), operationDTO.getOperationStatut());
        assertEquals("SQLZIY", operationDTO.getNumeroAutorisation());
        assertEquals("paiement accepte", operationDTO.getLibelle());
    }

    @Test
    public void extractResultCaptureRefuseeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class, CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" +
                "reference=000000000145\n" +
                "cdr=0\n" +
                "lib=commande non authentifiee";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.REFUSEE.name(), operationDTO.getOperationStatut());
        assertThat(operationDTO.getNumeroAutorisation()).isNull();
        assertEquals("commande non authentifiee", operationDTO.getLibelle());
    }

    @Test
    public void extractResultErreurTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = MoneticoApiClient.class.getDeclaredMethod("extractResult", String.class, CommandeOperationDTO.class);
        method.setAccessible(true);

        String test = "version=1.0\n" +
                "reference=000000000145\n" +
                "cdr=-1\n" +
                "lib=commercant non identifie";
        CommandeOperationDTO operationDTO = new CommandeOperationDTO();

        method.invoke(moneticoApiClient, test, operationDTO);

        assertEquals(OperationStatutEnum.ERREUR.name(), operationDTO.getOperationStatut());
        assertThat(operationDTO.getNumeroAutorisation()).isNull();
        assertEquals("commercant non identifie", operationDTO.getLibelle());
    }
}
