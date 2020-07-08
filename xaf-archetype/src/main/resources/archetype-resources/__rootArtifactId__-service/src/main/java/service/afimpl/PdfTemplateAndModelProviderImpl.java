#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import ${groupId}.shared.model.v1573825612706.ProjectDemandeDataDonneeDemandeurDTO;
import ${groupId}.shared.model.v1573825612706.ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO;
import ${groupId}.shared.model.v1573825612706.ProjectDemandeDataDonneeEntrepriseorigineDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import ${groupId}.shared.enums.${artifactIdCamelCase}DemandeStatutEnum;
import ${groupId}.shared.model.v1573825612706.ContenuProjectDemandeDTO;
import ${groupId}.shared.util.${artifactIdCamelCase}Utils;

/**
 * 
 * @author mpavone
 * 
 *         Permet à la démarche d'indiquer à af-back quel template utiliser pour
 *         générer un PDF pour une certaine demande, ainsi que le modèle associé
 *         à ce template.
 *
 */
@Component
public class PdfTemplateAndModelProviderImpl implements PdfTemplateAndModelProvider {

	private static final DateFormat FRENCH_DATE_FORMAT = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	private MotifsCache motifsCache;

	@Autowired
	private PaysCache paysCache;

	@Autowired
	private UtilisateursCache utilisateursCache;

	@Override
	public PdfTemplateAndModelDTO getTemplateAndModel(DemandeDTO demande, PdfTypeEnum pdfType) {
		return getCommonModelForPreviewOrFinalFile(demande, demande.getDernierStatut().getLibelle(),
				demande.getDernierStatut().getCommentaire(), demande.getDernierStatut().getTexteAEnvoyer(), pdfType);
	}

	@Override
	public PdfTemplateAndModelDTO getTemplateAndModelForPreview(DemandeDTO demande, String statutSuivant, String codeMotif,
																String langue, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {
		return getCommonModelForPreviewOrFinalFile(demande, statutSuivant, commentaire, texteAEnvoyer, pdfType);
	}

	/**
	 * Cette methode factorise la preview et la sauvegarde du model final étant
	 * donné qu'il n'y a pas de différence entre ces deux fichiers
	 */
	private PdfTemplateAndModelDTO getCommonModelForPreviewOrFinalFile(DemandeDTO demande, String statutSuivant, String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {
		PdfTemplateAndModelDTO dto = getTemplateAndModelGeneric(demande, statutSuivant, "fr", commentaire, texteAEnvoyer);

		if (PdfTypeEnum.COURRIER.equals(pdfType)) {
			getTemplateAndModelForCourrier(demande, dto, statutSuivant);
		} else {
			getTemplateAndModelForJustificatif(demande, dto, statutSuivant);
		}

		return dto;
	}

	private PdfTemplateAndModelDTO getTemplateAndModelForCourrier(DemandeDTO demande, PdfTemplateAndModelDTO dto, String statutSuivant) {
		String filename = "Courrier_";
		if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statutSuivant)) {
			dto.setTemplateFilename("DemandeInfoCompl.docx");
			filename += "Info_";
		} else if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name().equals(statutSuivant)) {
			dto.setTemplateFilename("DemandeValidee.docx");
		} else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutSuivant)) {
			dto.setTemplateFilename("DemandeRefusee.docx");
		}
		dto.setFilename(filename + demande.getIdentifiant() + "_");

		return dto;
	}

	private PdfTemplateAndModelDTO getTemplateAndModelForJustificatif(DemandeDTO demande, PdfTemplateAndModelDTO dto, String statutSuivant) {

		dto.setFilename("Justificatif_" + demande.getIdentifiant() + "_");
		if (${artifactIdCamelCase}DemandeStatutEnum.VALIDEE.name().equals(statutSuivant)) {
			dto.setTemplateFilename("JustificatifDemandeValidee.docx");
		} else if (${artifactIdCamelCase}DemandeStatutEnum.REFUSEE.name().equals(statutSuivant)) {
			dto.setTemplateFilename("JustificatifDemandeRefusee.docx");
		} else if (${artifactIdCamelCase}DemandeStatutEnum.EN_ATTENTE_COMPL.name().equals(statutSuivant)) {
			dto.setTemplateFilename("JustificatifDemandeInfoCompl.docx");
		}

		return dto;
	}

	private PdfTemplateAndModelDTO getTemplateAndModelGeneric(DemandeDTO demande, String codeMotif, String langue, String commentaire, String texteAEnvoyer) {

		ContenuProjectDemandeDTO contenuDemande = ${artifactIdCamelCase}Utils.getContenuDemande(demande);

		ProjectDemandeDataDonneeEntrepriseorigineDTO entrepriseorigineDTO = contenuDemande.getDonnee().getEntrepriseorigine();
		ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO adresseDTO = entrepriseorigineDTO.getAdresse();
		ProjectDemandeDataDonneeDemandeurDTO demandeurDTO = contenuDemande.getDonnee().getDemandeur();

		Map<String, Object> model = new HashMap<>();
		model.put("dateCourante", FRENCH_DATE_FORMAT.format(new Date()));
		model.put("identifiant", demande.getIdentifiant());
		String nom = utilisateursCache.get(demande.getAgentAffecteId()).getNomUsage();
		if(StringUtils.isEmpty(nom)) {
			nom = utilisateursCache.get(demande.getAgentAffecteId()).getNom();
		}
		model.put("nomAgent", nom);
		model.put("refCourrier", demande.getCourrierRefInterne());
		model.put("adresseLigne1", adresseDTO.getLigne1());
		model.put("adresseLigne2", adresseDTO.getLigne2());
		model.put("adresseLigne3", adresseDTO.getLigne3());
		model.put("adresseComplete", genererAdresseComplete(adresseDTO));
		model.put("codePostal", adresseDTO.getCodePostal());
        model.put("ville", adresseDTO.getVille());
        model.put("pays", paysCache.get(adresseDTO.getPays(), "fr").getNom());
        model.put("raisonSociale", entrepriseorigineDTO.getRaisonsociale());
        model.put("prenom", demandeurDTO.getPrenom());
        model.put("nom", demandeurDTO.getNom());
        model.put("titre",demandeurDTO.getTitre());

		String motif = "";
		if (StringUtils.isNotBlank(codeMotif) && motifsCache.getMotif(codeMotif, "fr") != null) {
			motif = motifsCache.getMotif(codeMotif, "fr").getLibelle();
		}
		model.put("motif", motif);
		model.put("commentaire", commentaire);
		model.put("texteAEnvoyer", texteAEnvoyer);

		if (demande.getCourrierDateReception() != null) {
			model.put("dateReception", DATE_FORMAT.format(demande.getCourrierDateReception()));
		}

		PdfTemplateAndModelDTO dto = new PdfTemplateAndModelDTO();
		dto.setModel(model);

		return dto;
	}

	@Override
	public PdfOptions getPdfOptions() {
		PdfOptions pdfOptions = PdfOptions.create();
		pdfOptions.fontProvider(new IFontProvider() {

			@Override
			public Font getFont(String familyName, String encoding, float size, int style, Color color) {
				try {
					if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.NORMAL) {
						BaseFont baseFont =
								BaseFont.createFont("/static/fonts/TIMES.TTF", encoding, BaseFont.EMBEDDED);
						return new Font(baseFont, size, style, color);

					} else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLD) {
						BaseFont baseFont =
								BaseFont.createFont("/static/fonts/TIMESBD.TTF", encoding, BaseFont.EMBEDDED);
						return new Font(baseFont, size, style, color);

					} else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.BOLDITALIC) {
						BaseFont baseFont =
								BaseFont.createFont("/static/fonts/TIMESBI.TTF", encoding, BaseFont.EMBEDDED);
						return new Font(baseFont, size, style, color);

					} else if (familyName.equalsIgnoreCase("Times New Roman") && style == Font.ITALIC) {
						BaseFont baseFont =
								BaseFont.createFont("/static/fonts/TIMESI.TTF", encoding, BaseFont.EMBEDDED);
						return new Font(baseFont, size, style, color);

					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				return FontFactory.getFont(familyName, encoding, size, style, color);
			}
		});

		return pdfOptions;
	}

	private String genererAdresseComplete(ProjectDemandeDataDonneeEntrepriseorigineAdresseDTO adresseDTO) {
		String adresseComplete = adresseDTO.getLigne1();
		if (!StringUtils.isEmpty(adresseDTO.getLigne2())) {
			adresseComplete += "${symbol_escape}n" + adresseDTO.getLigne2();
		}
		if (!StringUtils.isEmpty(adresseDTO.getLigne3())) {
			adresseComplete += "${symbol_escape}n" + adresseDTO.getLigne3();
		}
		return adresseComplete;
	}

}
