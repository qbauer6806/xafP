#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import mc.gouv.af.back.cache.PaysCache;
import mc.gouv.af.back.cache.PaysCacheImpl;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.servicerest.pays.model.PaysBean;
import mc.gouv.${artifactIdLower}.service.${artifactIdCamelCase}DataService;
import mc.gouv.${artifactIdLower}.shared.dto.CalculAideDTO;
import mc.gouv.${artifactIdLower}.shared.dto.SuiviComptableDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ProjectDemandeDataUsagerDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ProjectDemandeDataVehiculeDTO;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;

@Component
public class CgdPdfModelProvider implements PdfModelProvider {

    private final static String ${artifactIdUpper}_CGD_XMLNS_URI = "http://${artifactIdLower}.project";
    private final static String ${artifactIdUpper}_CGD_XMLNS_XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
    private final static String CGD_ROOT_XML_NODE_NAME = "demande";

    @Autowired
    private ${artifactIdCamelCase}DataService dataService;

    @Autowired
    private PaysCache paysCache;

    @Override
    public String xmlModelGenerator(DemandeDTO demandeDTO) throws Exception {

        ContenuProjectDemandeDTO contenuDemandeDTO = ${artifactIdCamelCase}Utils.getContenuDemande(demandeDTO);

        CalculAideDTO calculaideDTO = getCalculeAideDTO(demandeDTO.getPkDemandes());

        String xmlDocument = "";
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();
        Element root = createRootElement(doc);

        root.appendChild(createChildNode(doc, "numerodemande", demandeDTO.getIdentifiant()));
        root.appendChild(createChildNode(doc, "datedemande", getSimpleDate(demandeDTO.getDateCreation().toString())));
        root.appendChild(getDemandeurNode(contenuDemandeDTO.getUsager(), doc));
        root.appendChild(getVehiculeNode(contenuDemandeDTO.getVehicule(), doc));

        Element montantsubvention = createParentNode(doc, "montantsubvention");
        montantsubvention.appendChild(
                createChildNode(doc, "typevehicule", contenuDemandeDTO.getDonnee().getVehiculetypetous().libelle));
        montantsubvention.appendChild(
                createChildNode(doc, "primetaxi",
                        ${artifactIdCamelCase}Utils.convertBigDecimalToString(calculaideDTO.getPrimeTaxi())));

        montantsubvention
                .appendChild(
                        createChildNode(doc, "emmission", ${artifactIdCamelCase}Utils.getVehiculeEmissionLibelle(contenuDemandeDTO)));
        montantsubvention.appendChild(createChildNode(doc, "montantForfaitaire",
                ${artifactIdCamelCase}Utils.convertBigDecimalToString(calculaideDTO.getPrimeForfaitaire())));
        root.appendChild(montantsubvention);

        root.appendChild(getCalculaide(calculaideDTO, doc, true));

        root.appendChild(getAvisDirection(doc, calculaideDTO.getMontantAide().toString(), demandeDTO.getPkDemandes()));
        root.appendChild(createCgdCommentaire(doc, calculaideDTO.getCommentCGD()));
        doc.appendChild(root);
        xmlDocument = prepareXML(doc);
        return xmlDocument;

    }

    private String prepareXML(Document doc) throws TransformerException, IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

        DOMSource dom = new DOMSource(doc);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(dom, new StreamResult(bos));
        bos.close();
        return new String(bos.toByteArray());

    }

    private Element createRootElement(final Document doc) {
        Element root = doc.createElement(CGD_ROOT_XML_NODE_NAME);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", ${artifactIdUpper}_CGD_XMLNS_URI);
        root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", ${artifactIdUpper}_CGD_XMLNS_XSI_URI);
        return root;
    }

    private Element createParentNode(final Document doc, final String nodeName) {
        return doc.createElement(nodeName);
    }

    private Element createChildNode(final Document doc, final String nodeName, final String nodeText) {

        Element node = doc.createElement(nodeName);

        node.appendChild(
                doc.createTextNode(!StringUtils.isBlank(nodeText) ? nodeText : " "));
        return node;
    }

    private Element createCgdCommentaire(final Document doc, final String nodeText) {
        Element node = doc.createElement("commentCGD");
        node.appendChild(doc.createTextNode(!StringUtils.isBlank(nodeText) ? nodeText : " "));
        return node;
    }

    private CalculAideDTO getCalculeAideDTO(Integer demandeID) {
        return dataService.getCalculAideDTO(demandeID);
    }

    private SuiviComptableDTO getSuiviComptableDTO(Integer demandeID) {
        return dataService.getSuiviComptableDTO(demandeID);
    }

    private Element getDemandeurNode(final ProjectDemandeDataUsagerDTO usager, final Document doc) {
        Element demandeur = createParentNode(doc, "demandeur");

        demandeur.appendChild(createChildNode(doc, "titre", usager.getTitre().libelle));
        demandeur.appendChild(createChildNode(doc, "telephone", usager.getTelephone()));
        demandeur.appendChild(createChildNode(doc, "mail", usager.getMail()));
        demandeur.appendChild(createChildNode(doc, "nom", usager.getNom()));
        demandeur.appendChild(createChildNode(doc, "prenom", usager.getPrenom()));
        demandeur.appendChild(createChildNode(doc, "raisonsocial", usager.getRaisonsociale()));
        demandeur.appendChild(createChildNode(doc, "adresse", usager.getAdresse().getLigne1()));
        demandeur.appendChild(createChildNode(doc, "codepostal", usager.getAdresse().getCodePostal()));
        demandeur.appendChild(createChildNode(doc, "vile", usager.getAdresse().getVille()));
        demandeur.appendChild(createChildNode(doc, "pays",
                ((PaysBean) ((PaysCacheImpl) paysCache).get(usager.getAdresse().getPays(), "fr")).getNom()));

        return demandeur;
    }

    private Element getVehiculeNode(final ProjectDemandeDataVehiculeDTO vehiculeDTO, final Document doc) {
        Element vehicule = createParentNode(doc, "vehicule");

        vehicule.appendChild(createChildNode(doc, "immat", vehiculeDTO.getNumeroimmat()));
        vehicule.appendChild(createChildNode(doc, "marque", vehiculeDTO.getMarque()));
        vehicule.appendChild(createChildNode(doc, "genre", vehiculeDTO.getGenre()));
        vehicule.appendChild(
                createChildNode(doc, "datecirculation", getSimpleDate(vehiculeDTO.getDatemiseencirculation())));
        vehicule.appendChild(createChildNode(doc, "nombrekilometre", vehiculeDTO.getNombrekm()));
        vehicule.appendChild(createChildNode(doc, "datefacture", getSimpleDate(vehiculeDTO.getDatefacture())));
        return vehicule;
    }

    private Element getCalculaide(final CalculAideDTO dto, final Document doc, boolean show) {

        Element calculaide = createParentNode(doc, "calculaide");

        calculaide.appendChild(createChildNode(doc, "prixbasvehicule",
                show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrixBasVehicule()) : "0,00"));

        calculaide.appendChild(
                createChildNode(doc, "remise",
                        show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getRemiseDeduire()) : "0,00"));

        calculaide.appendChild(createChildNode(doc, "locatiobabtterie",
                show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getMontantBatterie()) : "0,00"));

        calculaide.appendChild(
                createChildNode(doc, "tva",
                        show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getTva()) : "0,00"));

        calculaide.appendChild(
                createChildNode(doc, "prixtotal",
                        show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrixTotalVehicule()) : "0,00"));

        calculaide.appendChild(createChildNode(doc, "applicationpourcentage",
                show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getApplicationPourcentage()) : "0,00"));

        calculaide.appendChild(createChildNode(doc, "primeSimule",
                show ? ${artifactIdCamelCase}Utils.convertBigDecimalToString(dto.getPrimeCalcule()) : "0,00"));

        return calculaide;
    }

    private Element getAvisDirection(final Document doc, final String montant, final Integer demandeID) {

        SuiviComptableDTO suiviComptableDTO = getSuiviComptableDTO(demandeID);
        Element avisdirection = createParentNode(doc, "avisdirection");
        avisdirection.appendChild(createChildNode(doc, "montant", montant));
        avisdirection.appendChild(createChildNode(doc, "exercice", suiviComptableDTO.getExercice()));
        avisdirection.appendChild(createChildNode(doc, "article", suiviComptableDTO.getArticle()));
        avisdirection.appendChild(createChildNode(doc, "fed", suiviComptableDTO.getFed()));
        avisdirection.appendChild(createChildNode(doc, "nordre", suiviComptableDTO.getNumeroOrdre()));

        return avisdirection;

    }

    private String getSimpleDate(String fullDate) {
        if (StringUtils.isBlank(fullDate)) {
            return "";
        }

        String dateRegex = "(${symbol_escape}${symbol_escape}d{4})-(${symbol_escape}${symbol_escape}d{2})-(${symbol_escape}${symbol_escape}d{2})";
        String simpleDate = "";
        Matcher matcher = Pattern.compile(dateRegex).matcher(fullDate);

        if (matcher.find()) {
            simpleDate = String.format("%s/%s/%s", matcher.group(3), matcher.group(2), matcher.group(1));
        }

        return simpleDate;
    }

}
