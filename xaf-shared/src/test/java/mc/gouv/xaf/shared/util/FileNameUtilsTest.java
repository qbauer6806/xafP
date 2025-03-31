package mc.gouv.xaf.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileNameUtilsTest {

    @Test
    void shouldReturnSameName_whenFilenameIsNull() {
        String result = FileNameUtils.getSafeFileName(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnSameName_whenFilenameIsEmpty() {
        String result = FileNameUtils.getSafeFileName("");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReplaceAccentsAndSpecialCharacters() {
        String filename = "dôssîer spéçîal@1234.pdf";
        String expected = "dossier_special_1234.pdf";

        String result = FileNameUtils.getSafeFileName(filename);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldKeepAllowedCharacters() {
        String filename = "file-name_123.txt";

        String result = FileNameUtils.getSafeFileName(filename);

        assertThat(result).isEqualTo(filename);
    }

    @Test
    void shouldReplaceForbiddenCharactersWithUnderscores() {
        String filename = "file@name#test$.pdf";
        String expected = "file_name_test_.pdf";

        String result = FileNameUtils.getSafeFileName(filename);

        assertThat(result).isEqualTo(expected);
    }
}
