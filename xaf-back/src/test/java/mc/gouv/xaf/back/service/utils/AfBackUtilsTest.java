package mc.gouv.xaf.back.service.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AfBackUtilsTest {

    @Test
    public void parseDoubleTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("123.45");
        assertEquals(123.45, parsed);
    }

    @Test
    public void parseDoubleVirguleTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("123,45");
        assertEquals(123.45, parsed);
    }

    @Test
    public void parseDoubleABCTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("abc");
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleVideTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("");
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleNullTest() {
        Double parsed = AfBackUtils.parseDoubleSafe(null);
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleBlancTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("   \t    \n ");
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleBlancEtNombreTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1  23\n456\t7.89");
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleDeuxVirgulesTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1,2,3");
        assertEquals(0.0, parsed);
    }

    @Test
    public void parseDoubleDeuxPointsTest() {
        Double parsed = AfBackUtils.parseDoubleSafe("1.2.3");
        assertEquals(0.0, parsed);
    }

    @Test
    public void formatDoubleToCurrencyTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000.5);
        assertEquals("1 000,50 €", formatted);
    }

    @Test
    public void formatDoubleToCurrencyZeroTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(0.0);
        assertEquals("0,00 €", formatted);
    }

    @Test
    public void formatDoubleToCurrencyMillionTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000000.0);
        assertEquals("1 000 000,00 €", formatted);
    }

    @Test
    public void formatDoubleToCurrencyENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000.5, "en");
        assertEquals("€1,000.50", formatted);
    }

    @Test
    public void formatDoubleToCurrencyZeroENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(0.0, "en");
        assertEquals("€0.00", formatted);
    }

    @Test
    public void formatDoubleToCurrencyMillionENTest() {
        String formatted = AfBackUtils.formatDoubleToCurrency(1000000.0, "en");
        assertEquals("€1,000,000.00", formatted);
    }

}
