

SET search_path = ${artifactIdUpper}, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

CREATE SEQUENCE dem_access_pk_access_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_access (
    pk_access integer DEFAULT nextval('dem_access_pk_access_seq'),
    active boolean NOT NULL,
    contenu character varying(10000) NOT NULL,
    date_creation timestamp without time zone NOT NULL,
    date_dermodif timestamp without time zone NOT NULL,
    fk_demarcheid character varying(128) NOT NULL,
    usager_id integer NOT NULL
);


ALTER TABLE ${artifactIdUpper}.dem_access OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_pk_demandes_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes (
    pk_demandes integer DEFAULT nextval('dem_demandes_pk_demandes_seq'),
    agent_affecte_id character varying(128),
    canal character varying(30) NOT NULL,
    contenu character varying(10000) NOT NULL,
    courrier_date_reception timestamp without time zone,
    courrier_ref_interne character varying(256),
    cree_par_agent_id character varying(128),
    date_creation timestamp without time zone NOT NULL,
    date_dermodif timestamp without time zone NOT NULL,
    identifiant character varying(30) NOT NULL,
    langue character varying(2),
    observations character varying(10000),
    fk_dernier_statut integer,
    fk_access integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_complements_pk_demandescomplements_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_complements (
    pk_demandescomplements integer DEFAULT nextval('dem_demandes_complements_pk_demandescomplements_seq'),
    agent_id character varying(128) NOT NULL,
    code_motif character varying(128) NOT NULL,
    date_creation timestamp without time zone NOT NULL,
    date_reponse timestamp without time zone,
    question character varying(8000),
    reponse character varying(8000),
    reponse_agent_id character varying(128),
    reponse_usager_id integer,
    statut character varying(64) NOT NULL,
    fk_demandes integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_complements OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_complements_files_pk_demandescomplementsfiles_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_complements_files (
    pk_demandescomplementsfiles integer DEFAULT nextval('dem_demandes_complements_files_pk_demandescomplementsfiles_seq'),
    meta character varying(512),
    name character varying(1024) NOT NULL,
    url character varying(1024) NOT NULL,
    fk_demandescomplements integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_complements_files OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_courriers_pk_demandescourriers_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_courriers (
    pk_demandescourriers integer DEFAULT nextval('dem_demandes_courriers_pk_demandescourriers_seq'),
    date_creation timestamp without time zone NOT NULL,
    date_printed timestamp without time zone,
    identifiant character varying(128),
    meta character varying(512),
    name character varying(1024) NOT NULL,
    url character varying(1024) NOT NULL,
    fk_demandes integer,
    fk_demandesstatuts integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_courriers OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_data_pk_demandesdata_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_data (
    pk_demandesdata integer DEFAULT nextval('dem_demandes_data_pk_demandesdata_seq'),
    key character varying(256) NOT NULL,
    value character varying(10000),
    fk_demandes integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_data OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_files_pk_demandesfiles_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_files (
    pk_demandesfiles integer DEFAULT nextval('dem_demandes_files_pk_demandesfiles_seq'),
    meta character varying(512),
    name character varying(1024) NOT NULL,
    url character varying(1024) NOT NULL,
    fk_demandes integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_files OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_historique_pk_demandeshistorique_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_historique (
    pk_demandeshistorique integer DEFAULT nextval('dem_demandes_historique_pk_demandeshistorique_seq'),
    agent_id character varying(128),
    contenu character varying(10000) NOT NULL,
    date timestamp without time zone NOT NULL,
    usager_id integer,
    fk_demandes integer NOT NULL,
    fk_statut integer NOT NULL
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_historique OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_demandes_statuts_pk_demandesstatuts_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_statuts (
    pk_demandesstatuts integer DEFAULT nextval('dem_demandes_statuts_pk_demandesstatuts_seq'),
    agent_id character varying(128),
    code_motif character varying(128),
    commentaire character varying(8000),
    date timestamp without time zone NOT NULL,
    libelle character varying(64) NOT NULL,
    usager_id integer,
    fk_demandes integer
);


ALTER TABLE ${artifactIdUpper}.dem_demandes_statuts OWNER TO ${artifactIdUpper};

CREATE TABLE ${artifactIdUpper}.dem_demarches (
    pk_demarcheid character varying(128) NOT NULL,
    nom character varying(256) NOT NULL,
	email_service character varying(256) NOT NULL,
    email_service_nom character varying(256) NOT NULL,
    email_replyto character varying(256) NOT NULL,
    email_replyto_nom character varying(256) NOT NULL,
    email_from character varying(256) NOT NULL,
    email_from_nom character varying(256) NOT NULL
);


ALTER TABLE ${artifactIdUpper}.dem_demarches OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_motifs_pk_motifs_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_motifs (
    pk_motifs integer DEFAULT nextval('dem_motifs_pk_motifs_seq'),
	fk_demarcheid character varying(128) NOT NULL,
	libelle character varying(256) NOT NULL,
    code character varying(128) NOT NULL,
    statut character varying(64) NOT NULL,
	langue character varying(2) NOT NULL,
    date_archive timestamp without time zone,
    commentaire_prerempli character varying(2048)
);


ALTER TABLE ${artifactIdUpper}.dem_motifs OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_periodes_ouverture_pk_periodesouverture_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_periodes_ouverture (
    pk_periodesouverture integer DEFAULT nextval('dem_periodes_ouverture_pk_periodesouverture_seq'),
    date_debut timestamp without time zone NOT NULL,
    date_fin timestamp without time zone,
    fk_demarcheid character varying(128) NOT NULL
);


ALTER TABLE ${artifactIdUpper}.dem_periodes_ouverture OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_templates_pk_templates_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_templates (
    pk_templates integer DEFAULT nextval('dem_templates_pk_templates_seq'),
    fk_demarcheid character varying(128) NOT NULL,
    code character varying(256) NOT NULL,
    contenu character varying(10000) NOT NULL,
    langue character varying(2)
);


ALTER TABLE ${artifactIdUpper}.dem_templates OWNER TO ${artifactIdUpper};

CREATE SEQUENCE dem_usagers_courrier_pk_usagerscourrier_seq START 1000000001;

CREATE TABLE ${artifactIdUpper}.dem_usagers_courrier (
    pk_usagerscourrier integer DEFAULT nextval('dem_usagers_courrier_pk_usagerscourrier_seq'),
    adresse1 character varying(128) NOT NULL,
    adresse2 character varying(128),
    adresse_complement character varying(128),
    code_postal character varying(10) NOT NULL,
    date_creation timestamp without time zone NOT NULL,
    date_dermodif timestamp without time zone NOT NULL,
    fk_demarcheid character varying(128) NOT NULL,
    email character varying(256),
    login character varying(20) NOT NULL,
    nom character varying(50) NOT NULL,
    pays character varying(2) NOT NULL,
    prenom character varying(20),
    raison_sociale character varying(100),
    telephone character varying(64),
    titre integer,
    ville character varying(50) NOT NULL
);


ALTER TABLE ${artifactIdUpper}.dem_usagers_courrier OWNER TO ${artifactIdUpper};

SELECT pg_catalog.setval('${artifactIdUpper}.dem_access_pk_access_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_complements_files_pk_demandescomplementsfiles_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_complements_pk_demandescomplements_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_courriers_pk_demandescourriers_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_data_pk_demandesdata_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_files_pk_demandesfiles_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_historique_pk_demandeshistorique_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_pk_demandes_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_demandes_statuts_pk_demandesstatuts_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_motifs_pk_motifs_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_periodes_ouverture_pk_periodesouverture_seq', 1, false);

SELECT pg_catalog.setval('${artifactIdUpper}.dem_templates_pk_templates_seq', 1, false);

ALTER TABLE ONLY ${artifactIdUpper}.dem_access
    ADD CONSTRAINT dem_access_pkey PRIMARY KEY (pk_access);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_complements_files
    ADD CONSTRAINT dem_demandes_complements_files_pkey PRIMARY KEY (pk_demandescomplementsfiles);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_complements
    ADD CONSTRAINT dem_demandes_complements_pkey PRIMARY KEY (pk_demandescomplements);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_courriers
    ADD CONSTRAINT dem_demandes_courriers_pkey PRIMARY KEY (pk_demandescourriers);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_data
    ADD CONSTRAINT dem_demandes_data_pkey PRIMARY KEY (pk_demandesdata);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_files
    ADD CONSTRAINT dem_demandes_files_pkey PRIMARY KEY (pk_demandesfiles);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_historique
    ADD CONSTRAINT dem_demandes_historique_pkey PRIMARY KEY (pk_demandeshistorique);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes
    ADD CONSTRAINT dem_demandes_pkey PRIMARY KEY (pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_statuts
    ADD CONSTRAINT dem_demandes_statuts_pkey PRIMARY KEY (pk_demandesstatuts);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demarches
    ADD CONSTRAINT dem_demarches_pkey PRIMARY KEY (pk_demarcheid);

ALTER TABLE ONLY ${artifactIdUpper}.dem_motifs
    ADD CONSTRAINT dem_motifs_pkey PRIMARY KEY (pk_motifs);

ALTER TABLE ONLY ${artifactIdUpper}.dem_periodes_ouverture
    ADD CONSTRAINT dem_periodes_ouverture_pkey PRIMARY KEY (pk_periodesouverture);

ALTER TABLE ONLY ${artifactIdUpper}.dem_templates
    ADD CONSTRAINT dem_templates_pkey PRIMARY KEY (pk_templates);

ALTER TABLE ONLY ${artifactIdUpper}.dem_usagers_courrier
    ADD CONSTRAINT dem_usagers_courrier_pkey PRIMARY KEY (pk_usagerscourrier);

CREATE UNIQUE INDEX ukfudh1tb813bkgdkolk6t9cd3k ON ${artifactIdUpper}.dem_templates USING btree (code, langue);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_complements
    ADD CONSTRAINT fk1dy34yj63erui43ymfaihpbfv FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes
    ADD CONSTRAINT fk7lgwktqs0m8r09235h22pxxrt FOREIGN KEY (fk_dernier_statut) REFERENCES ${artifactIdUpper}.dem_demandes_statuts(pk_demandesstatuts);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes
    ADD CONSTRAINT fk83j6el6xsxjm44dc38hradchf FOREIGN KEY (fk_access) REFERENCES ${artifactIdUpper}.dem_access(pk_access);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_files
    ADD CONSTRAINT fk86agnpvmtq5a3rl3l19xoxy5 FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_historique
    ADD CONSTRAINT fkct0esa54aqcmlpi4gbtsmvuss FOREIGN KEY (fk_statut) REFERENCES ${artifactIdUpper}.dem_demandes_statuts(pk_demandesstatuts);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_courriers
    ADD CONSTRAINT fkdtpmcp2tpn16fcbpplvq4a3hp FOREIGN KEY (fk_demandesstatuts) REFERENCES ${artifactIdUpper}.dem_demandes_statuts(pk_demandesstatuts);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_courriers
    ADD CONSTRAINT fkehgy33b5b9tnbeho5mrlyqir5 FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_data
    ADD CONSTRAINT fkh6qr92ie476cqvsg2dvs5tfr4 FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_statuts
    ADD CONSTRAINT fkhu956nk8subiuvv6fa4upe5w9 FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_historique
    ADD CONSTRAINT fkk5d0q2xvljbwm05aob03uv8lm FOREIGN KEY (fk_demandes) REFERENCES ${artifactIdUpper}.dem_demandes(pk_demandes);

ALTER TABLE ONLY ${artifactIdUpper}.dem_demandes_complements_files
    ADD CONSTRAINT fkpoq6aeqt7qx1n4phb18693sv8 FOREIGN KEY (fk_demandescomplements) REFERENCES ${artifactIdUpper}.dem_demandes_complements(pk_demandescomplements);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ${artifactIdUpper} TO ${artifactIdUpper};
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ${artifactIdUpper} TO ${artifactIdUpper};
