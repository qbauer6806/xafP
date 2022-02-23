package mc.gouv.xaf.back.data.es.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;

@Document(indexName = "#{@environment.getProperty('application.name')}", createIndex = false)
public class DemandeFileEsDTO {

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public static final String INDEX_FILES_JOIN_DOC = "fichiers";
    public static final String TYPE_FIELD = "fichiers.type";
    public static final String IDENTIFIANT_FIELD = "fichiers.identifiant";
    public static final String DATE_PRINTED_FIELD = "fichiers.datePrinted";

    public enum TYPE {
        PIECE_JOINTE,
        FICHIER_INTERNE,
        COMPLEMENT,
        COURRIER
    }

    private Fichiers fichiers;
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    private DemandeJoinFieldEsDTO demandeJoinField;
    
    @Id
    protected String identifiant;

    public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public void setDemandeJoinField(DemandeJoinFieldEsDTO demandeJoinField) {
        this.demandeJoinField = demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public DemandeFileEsDTO(String parent) {
        fichiers = new Fichiers();
        setDemandeJoinField(new DemandeJoinFieldEsDTO(INDEX_FILES_JOIN_DOC, parent));
    }

    public DemandeFileEsDTO() {

    }

    public Fichiers getFichiers() {
        return fichiers;
    }

    public void setFichiers(Fichiers fichiers) {
        this.fichiers = fichiers;
    }

    public static class Fichiers {

        @NotNull
        protected String name;

        /**
         * Id unique, différent de la pkDemandeFile (généré à partir de l'url et nom) et utilisé par ES
         */
        @Id
        protected String id;

        @NotNull
        protected String url;
        protected String meta;
        private String content;
        private String language;
        private String type;
        private String statut;
        private Date dateCreation;
        private Integer pkDemande;
        private Integer pkDemandeFile;
        private String typedoc;
        private Date datePrinted;

        /**
         * Identifiant courrier (ref_interne sur la page gestioncourrier)
         */
        private String identifiant;

        /**
         * Identifiant de la demande
         */
        private String identifiantDemande;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return url.replace("/", "-").replace("\\", "-");
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }

        public Date getDateCreation() {
            return dateCreation;
        }

        public void setDateCreation(Date dateCreation) {
            this.dateCreation = dateCreation;
        }

        public Integer getPkDemande() {
            return pkDemande;
        }

        public void setPkDemande(Integer pkDemande) {
            this.pkDemande = pkDemande;
        }

        public Integer getPkDemandeFile() {
            return pkDemandeFile;
        }

        public void setPkDemandeFile(Integer pkDemandeFile) {
            this.pkDemandeFile = pkDemandeFile;
        }

        public String getIdentifiant() {
            return identifiant;
        }

        public void setIdentifiant(String identifiant) {
            this.identifiant = identifiant;
        }

        public String getIdentifiantDemande() {
            return identifiantDemande;
        }

        public void setIdentifiantDemande(String identifiantDemande) {
            this.identifiantDemande = identifiantDemande;
        }

		public Date getDatePrinted() {
			return datePrinted;
		}

		public void setDatePrinted(Date datePrinted) {
			this.datePrinted = datePrinted;
		}

        public String getTypedoc() {
            return typedoc;
        }

        public void setTypedoc(String typedoc) {
            this.typedoc = typedoc;
        }
    }

}
