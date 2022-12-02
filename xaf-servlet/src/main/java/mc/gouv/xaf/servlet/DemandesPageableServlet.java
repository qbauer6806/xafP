package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DemandesPageableServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 5580063612318092075L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesPageableServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOGGER.info("====================== /demandespage doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
             AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération des paramètres
        PageParamDTO paramDTO = new PageParamDTO();
        try {
            String pageNbr = request.getParameter(RequestConstant.PAGE_PARAM);
            if (StringUtils.isNotBlank(pageNbr)) {
                paramDTO.setPage(Integer.parseInt(pageNbr));
            }
            String size = request.getParameter(RequestConstant.SIZE_PARAM);
            if (StringUtils.isNotBlank(size)) {
                paramDTO.setSize(Integer.parseInt(size));
            }
        } catch (NumberFormatException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Problème lors du parsing des paramètres");
            return;
        }

        String sort = request.getParameter(RequestConstant.SORT_PARAM);
        if (StringUtils.isNotBlank(sort)) {
            paramDTO.setSort(sort);
        }
        String direction = request.getParameter(RequestConstant.DIRECTION_PARAM);
        if (StringUtils.isNotBlank(direction)) {
            paramDTO.setDirection(direction);
        }
        String status = request.getParameter(RequestConstant.STATUS_PARAM);
        if (StringUtils.isNotBlank(status)) {
            paramDTO.setStatus(status);
        }
        String lang = request.getParameter(RequestConstant.LANG_PARAM);
        if (StringUtils.isNotBlank(lang)) {
            paramDTO.setLang(lang);
        }

        LOGGER.info("Récupération des demandes pour l'usager dont usagerId = {}", usagerId);
        Page<DemandeDTO> page = getAfApiClient().getDemandesPageable(usagerId, paramDTO);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(page);
            response.setContentType(MediaType.APPLICATION_JSON);
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            response.setStatus(HttpStatus.SC_OK);
        } catch (IOException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "DemandesPageableServlet - Une erreur est survenue lors de l'appel à la méthode GET");
        }

        LOGGER.info("====================== FIN /demandespage doGet()");

    }
}
