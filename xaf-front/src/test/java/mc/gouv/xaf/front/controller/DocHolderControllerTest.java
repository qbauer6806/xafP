package mc.gouv.xaf.front.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import mc.gouv.xaf.front.dto.DocHolderFilePostDTO;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.DelegatingServletInputStream;

@ExtendWith(MockitoExtension.class)
class DocHolderControllerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private XafFrontserverUtils servletUtilsMocked;
    @InjectMocks
    private DocHolderTypedocController docHolderTypedocController;
    @InjectMocks
    private DocHolderFileController docHolderFileController;

    @Test
    void failOnUserNotLoggedTest() throws ServletException, IOException {
        when(servletUtilsMocked.getLoggedUser(any())).thenReturn(null);
        when(servletUtilsMocked.logAndSendError(any(), anyInt(), anyString()))
                .thenReturn(ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).build());

        ResponseEntity<?> responseEntity = docHolderTypedocController.doGet(request);
        Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, responseEntity.getStatusCodeValue());
    }

    private static Stream<Arguments> emptyOrInvalidFileParameters() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(null, null, ""),
                Arguments.of(null, "", null),
                Arguments.of("", "", null),
                Arguments.of("", "  ", null),
                Arguments.of("", "", ""),
                Arguments.of("  ", "  ", "  ")
        );
    }

    @ParameterizedTest
    @MethodSource("emptyOrInvalidFileParameters")
    void failOnBadParameters(String url, String typedoc, String preferredName) throws IOException {
        UsagerInfosDTO usagerInfosDTO = mock(UsagerInfosDTO.class);

        when(servletUtilsMocked.getLoggedUser(any())).thenReturn(usagerInfosDTO);
        when(servletUtilsMocked.logAndSendError(any(Logger.class), any(org.springframework.http.HttpStatus.class), anyString()))
                .thenReturn(ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).build());

        DocHolderFilePostDTO filePostDTO = new DocHolderFilePostDTO();
        filePostDTO.setUrl(url);
        filePostDTO.setTypedoc(typedoc);
        filePostDTO.setPreferredName(preferredName);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(filePostDTO);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
             DelegatingServletInputStream dsis = new DelegatingServletInputStream(bais)) {
            when(request.getInputStream()).thenReturn(dsis);

            ResponseEntity<?> responseEntity = docHolderFileController.doPost(request);
            Assertions.assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
        }
    }
}

