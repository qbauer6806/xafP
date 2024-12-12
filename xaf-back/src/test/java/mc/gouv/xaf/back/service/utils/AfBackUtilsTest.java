package mc.gouv.xaf.back.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AfBackUtilsTest {

    @InjectMocks
    private AfBackUtils afBackUtils;

    @Test
    void parseDoubleTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("123.45");
        assertEquals(123.45, parsed);
    }

    @Test
    void parseDoubleVirguleTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("123,45");
        assertEquals(123.45, parsed);
    }

    @Test
    void parseDoubleABCTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("abc");
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleVideTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("");
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleNullTest() {
        Double parsed = AfBackUtils.parseDoubleSafe(null);
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleBlancTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("   \t    \n ");
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleBlancEtNombreTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1  23\n456\t7.89");
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleDeuxVirgulesTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1,2,3");
        assertEquals(0.0, parsed);
    }

    @Test
    void parseDoubleDeuxPointsTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1.2.3");
        assertEquals(0.0, parsed);
    }

    @Test
    void formatDoubleToCurrencyTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000.5);
        assertEquals("1 000,50 €", formatted);
    }

    @Test
    void formatDoubleToCurrencyZeroTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(0.0);
        assertEquals("0,00 €", formatted);
    }

    @Test
    void formatDoubleToCurrencyMillionTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000000.0);
        assertEquals("1 000 000,00 €", formatted);
    }

    @Test
    void formatDoubleToCurrencyENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000.5, "en");
        assertEquals("€1,000.50", formatted);
    }

    @Test
    void formatDoubleToCurrencyZeroENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(0.0, "en");
        assertEquals("€0.00", formatted);
    }

    @Test
    void formatDoubleToCurrencyMillionENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000000.0, "en");
        assertEquals("€1,000,000.00", formatted);
    }

    @Test
    void logSafeTest() {
        String safe = "Safe String";
        String resultSafe = AfBackUtils.logSafe(safe);
        assertEquals(safe, resultSafe);
        String resultUnsafe = AfBackUtils.logSafe("Unsafe\nSt\rring");
        assertEquals("Unsafe_St_ring", resultUnsafe);
    }

    @Test
    void testGetStatusColorClassWhenStatutPuclicIsNullThenReturnDefautStatutColor() {
        String statusColorClass = afBackUtils.getStatusColorClass(null);
        assertEquals("default-status-color", statusColorClass);
    }

    @Test
    void testGetStatusColorClassWhenStatutPuclicIsValideeThenReturnStatutColorValidee() {
        String statusColorClass = afBackUtils.getStatusColorClass("VALIDEE");
        assertEquals("validee", statusColorClass);
    }

}
