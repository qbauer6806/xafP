CREATE SEQUENCE ${artifactIdLower}.dem_recherche_champ_config_seq START 1;
CREATE SEQUENCE ${artifactIdLower}.dem_recherche_cat_config_seq START 1;


CREATE TABLE ${artifactIdLower}.dem_recherche_cat_config (
    id integer DEFAULT nextval('${artifactIdLower}.dem_recherche_cat_config_seq'),
    libelle character varying(10000) NOT NULL UNIQUE,
    editable boolean NOT NULL
);

CREATE TABLE ${artifactIdLower}.dem_recherche_champ_config (
    id integer DEFAULT nextval('${artifactIdLower}.dem_recherche_champ_config_seq'),
    enabled boolean NOT NULL,
    cle character varying(10000) NOT NULL UNIQUE,
    libelle character varying(10000) NOT NULL,
    editable boolean NOT NULL,
    fk_categorie integer
);



ALTER TABLE ONLY ${artifactIdLower}.dem_recherche_cat_config
    ADD CONSTRAINT dem_recherche_cat_config_pkey PRIMARY KEY (id);
    
ALTER TABLE ONLY ${artifactIdLower}.dem_recherche_champ_config
    ADD CONSTRAINT dem_recherche_champ_config_pkey PRIMARY KEY (id);    

ALTER TABLE ONLY ${artifactIdLower}.dem_recherche_champ_config
    ADD CONSTRAINT recherche_champ_config_cat_const FOREIGN KEY (fk_categorie) REFERENCES ${artifactIdLower}.dem_recherche_cat_config(id);

SELECT pg_catalog.setval('${artifactIdLower}.dem_recherche_champ_config_seq', 1, false);
SELECT pg_catalog.setval('${artifactIdLower}.dem_recherche_cat_config_seq', 1, false);