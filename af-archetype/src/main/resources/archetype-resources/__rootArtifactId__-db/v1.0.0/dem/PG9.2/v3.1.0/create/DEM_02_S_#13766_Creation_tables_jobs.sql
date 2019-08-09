CREATE SEQUENCE ${artifactIdLower}.dem_jobs_seq START 1;


CREATE TABLE ${artifactIdLower}.dem_jobs (
    id integer DEFAULT nextval('${artifactIdLower}.dem_jobs_seq'),
    JOB_NAME character varying(256) NOT NULL,
    date_creation timestamp without time zone NOT NULL,
    date_dermodif timestamp without time zone NOT NULL,
    msg text NOT NULL,
    statut character varying(256) NOT NULL
);


ALTER TABLE ONLY ${artifactIdLower}.dem_jobs
    ADD CONSTRAINT dem_dem_jobs_pkey PRIMARY KEY (id);
    

SELECT pg_catalog.setval('${artifactIdLower}.dem_jobs_seq', 1, false);
