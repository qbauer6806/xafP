package mc.gouv.xaf.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;

/**
 * 
 * Servlet servant à récupérer les brouillons d'un usager de façon paginée.
 * 
 * @author qdeme
 *
 */
public class BrouillonsPageableServlet extends AbstractAfServlet {


	private static final long serialVersionUID = 6946764515064886781L;
	private static final Logger LOGGER = LoggerFactory.getLogger(BrouillonsPageableServlet.class);
    private static final String PAGE_PARAM = "page";
    private static final String SIZE_PARAM = "size";
    private static final String SORT_PARAM = "sort";
    private static final String DIRECTION_PARAM = "direction";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        LOGGER.info("====================== /brouillonspage doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération des paramètres
        PageParamDTO paramDTO = new PageParamDTO();
        String pageNbr = request.getParameter(PAGE_PARAM);
        if (StringUtils.isNotBlank(pageNbr)) {
            paramDTO.setPage(Integer.parseInt(pageNbr));
        }
        String size = request.getParameter(SIZE_PARAM);
        if (StringUtils.isNotBlank(size)) {
            paramDTO.setSize(Integer.parseInt(size));
        }
        String sort = request.getParameter(SORT_PARAM);
        if (StringUtils.isNotBlank(sort)) {
            paramDTO.setSort(sort);
        }
        String direction = request.getParameter(DIRECTION_PARAM);
        if (StringUtils.isNotBlank(direction)) {
            paramDTO.setDirection(direction);
        }

        LOGGER.info("Récupération des brouillons pour l'usager dont usagerId = {}", usagerId);
        Page<BrouillonDTO> page = getAfApiClient().getBrouillonsPageable(usagerId, paramDTO);

        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        String repJson = mapper.writeValueAsString(page);

        response.setContentType("application/json");
        IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());

        LOGGER.info("====================== FIN /brouillonspage doGet()");

    }
}
