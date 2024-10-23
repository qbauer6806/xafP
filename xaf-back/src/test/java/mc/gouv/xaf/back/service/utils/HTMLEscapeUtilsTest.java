package mc.gouv.xaf.back.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HTMLEscapeUtilsTest {

    @Test
    void escapeTextTest() {
        String txt = "coucou";
        String result = HTMLEscapeUtils.escape(txt);
        assertEquals(txt, result);
    }

    @Test
    void escapeNumbersTest() {
        String txt = "123";
        String result = HTMLEscapeUtils.escape(txt);
        assertEquals(txt, result);
    }

    @Test
    void escapeHTMLTest() {
        String txt = "<p>html</p>";
        String result = HTMLEscapeUtils.escape(txt);
        String expected = "&lt;p&gt;html&lt;/p&gt;";
        assertEquals(expected, result);
    }

    @Test
    void escapeJavaScriptTest() {
        String txt = "<script>alert(1);</script>";
        String result = HTMLEscapeUtils.escape(txt);
        String expected = "&lt;script&gt;alert(1)&semi;&lt;/script&gt;";
        assertEquals(expected, result);
    }

}
