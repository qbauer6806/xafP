# XAF 11.3.0 - Optimisation de la purge

## Optimisation de la purge

```
DB Change:
Executer le script sql suivant, en remplacant <TSCODE>

-- SEQUENCE: <TSCODE>.dem_purge_files_pk_purgefiles_seq
DROP TABLE IF EXISTS <TSCODE>.dem_purge_files;
DROP SEQUENCE IF EXISTS <TSCODE>.dem_purge_files_pk_purgefiles_seq;

CREATE SEQUENCE IF NOT EXISTS <TSCODE>.dem_purge_files_pk_purgefiles_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;


-- TABLE: <TSCODE>.dem_purge_files
CREATE TABLE IF NOT EXISTS <TSCODE>.dem_purge_files
(
    pk_purgefiles integer NOT NULL DEFAULT nextval('<TSCODE>.dem_purge_files_pk_purgefiles_seq'::regclass),
    url character varying(1024) COLLATE pg_catalog."default" NOT NULL,
    creationDate timestamp without time zone default now(),
    CONSTRAINT dem_demandes_purge_files_pkey PRIMARY KEY (pk_purgefiles)
);

DROP TABLE IF EXISTS <TSCODE>.QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_LOCKS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_TRIGGERS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS <TSCODE>.QRTZ_CALENDARS;

CREATE TABLE <TSCODE>.QRTZ_JOB_DETAILS
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    JOB_NAME          VARCHAR(200) NOT NULL,
    JOB_GROUP         VARCHAR(200) NOT NULL,
    DESCRIPTION       VARCHAR(250) NULL,
    JOB_CLASS_NAME    VARCHAR(250) NOT NULL,
    IS_DURABLE        BOOL         NOT NULL,
    IS_NONCONCURRENT  BOOL         NOT NULL,
    IS_UPDATE_DATA    BOOL         NOT NULL,
    REQUESTS_RECOVERY BOOL         NOT NULL,
    JOB_DATA          BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_TRIGGERS
(
    SCHED_NAME     VARCHAR(120) NOT NULL,
    TRIGGER_NAME   VARCHAR(200) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL,
    JOB_NAME       VARCHAR(200) NOT NULL,
    JOB_GROUP      VARCHAR(200) NOT NULL,
    DESCRIPTION    VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT       NULL,
    PREV_FIRE_TIME BIGINT       NULL,
    PRIORITY       INTEGER      NULL,
    TRIGGER_STATE  VARCHAR(16)  NOT NULL,
    TRIGGER_TYPE   VARCHAR(8)   NOT NULL,
    START_TIME     BIGINT       NOT NULL,
    END_TIME       BIGINT       NULL,
    CALENDAR_NAME  VARCHAR(200) NULL,
    MISFIRE_INSTR  SMALLINT     NULL,
    JOB_DATA       BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
        REFERENCES <TSCODE>.QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_SIMPLE_TRIGGERS
(
    SCHED_NAME      VARCHAR(120) NOT NULL,
    TRIGGER_NAME    VARCHAR(200) NOT NULL,
    TRIGGER_GROUP   VARCHAR(200) NOT NULL,
    REPEAT_COUNT    BIGINT       NOT NULL,
    REPEAT_INTERVAL BIGINT       NOT NULL,
    TIMES_TRIGGERED BIGINT       NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_CRON_TRIGGERS
(
    SCHED_NAME      VARCHAR(120) NOT NULL,
    TRIGGER_NAME    VARCHAR(200) NOT NULL,
    TRIGGER_GROUP   VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID    VARCHAR(80),
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_SIMPROP_TRIGGERS
(
    SCHED_NAME    VARCHAR(120)   NOT NULL,
    TRIGGER_NAME  VARCHAR(200)   NOT NULL,
    TRIGGER_GROUP VARCHAR(200)   NOT NULL,
    STR_PROP_1    VARCHAR(512)   NULL,
    STR_PROP_2    VARCHAR(512)   NULL,
    STR_PROP_3    VARCHAR(512)   NULL,
    INT_PROP_1    INT            NULL,
    INT_PROP_2    INT            NULL,
    LONG_PROP_1   BIGINT         NULL,
    LONG_PROP_2   BIGINT         NULL,
    DEC_PROP_1    NUMERIC(13, 4) NULL,
    DEC_PROP_2    NUMERIC(13, 4) NULL,
    BOOL_PROP_1   BOOL           NULL,
    BOOL_PROP_2   BOOL           NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_BLOB_TRIGGERS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    TRIGGER_NAME  VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA     BYTEA        NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
        REFERENCES <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_CALENDARS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    CALENDAR_NAME VARCHAR(200) NOT NULL,
    CALENDAR      BYTEA        NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
);


CREATE TABLE <TSCODE>.QRTZ_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME    VARCHAR(120) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
);

CREATE TABLE <TSCODE>.QRTZ_FIRED_TRIGGERS
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    ENTRY_ID          VARCHAR(95)  NOT NULL,
    TRIGGER_NAME      VARCHAR(200) NOT NULL,
    TRIGGER_GROUP     VARCHAR(200) NOT NULL,
    INSTANCE_NAME     VARCHAR(200) NOT NULL,
    FIRED_TIME        BIGINT       NOT NULL,
    SCHED_TIME        BIGINT       NOT NULL,
    PRIORITY          INTEGER      NOT NULL,
    STATE             VARCHAR(16)  NOT NULL,
    JOB_NAME          VARCHAR(200) NULL,
    JOB_GROUP         VARCHAR(200) NULL,
    IS_NONCONCURRENT  BOOL         NULL,
    REQUESTS_RECOVERY BOOL         NULL,
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
);

CREATE TABLE <TSCODE>.QRTZ_SCHEDULER_STATE
(
    SCHED_NAME        VARCHAR(120) NOT NULL,
    INSTANCE_NAME     VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT       NOT NULL,
    CHECKIN_INTERVAL  BIGINT       NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
);

CREATE TABLE <TSCODE>.QRTZ_LOCKS
(
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
);

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON <TSCODE>.QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON <TSCODE>.QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON <TSCODE>.QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON <TSCODE>.QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP);

CREATE INDEX IF NOT EXISTS DEM_STAT_STATUT_PUBLIC_IDX ON <TSCODE>.DEM_STATISTIQUES(STATUT_PUBLIC);

GRANT INSERT, SELECT, UPDATE, DELETE ON ALL TABLES IN SCHEMA <TSCODE> TO <TSCODE>;
ALTER DEFAULT PRIVILEGES IN SCHEMA <TSCODE> GRANT INSERT, SELECT, UPDATE, DELETE ON TABLES TO <TSCODE>;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA <TSCODE> TO <TSCODE>;


GRANT ALL ON SEQUENCE <TSCODE>.dem_purge_files_pk_purgefiles_seq TO <TSCODE>;
GRANT SELECT ON SEQUENCE <TSCODE>.dem_purge_files_pk_purgefiles_seq TO teamdev;

REVOKE ALL ON TABLE <TSCODE>.dem_purge_files FROM teamdev;
GRANT ALL ON TABLE <TSCODE>.dem_purge_files TO <TSCODE>;
GRANT SELECT ON TABLE <TSCODE>.dem_purge_files TO teamdev;
```


# XAF 11.1.0 - LOGDOMC Changes

## Fonctionnalité de modification d'une demande dans un état "EN ATTENTE DE TRAITEMENT"

```sql
	Code: XAF
	DB:
		ALTER TABLE logdomc.dem_demandes ADD COLUMN modification_timestamp bigint;
```

## Fonctionnalité liées au DLNUF - Utilisation d'une interface commune pour les données externes, enregistrement des données externes en DB pour comparaison fututre

```sql
	Code: XAF
	DB:
		ALTER TABLE logdomc.dem_demandes ADD COLUMN contenu_initial character varying(10000);
		ALTER TABLE logdomc.dem_brouillons ADD COLUMN contenu_initial character varying(10000);
```
### Parameters liés aux données externes

Pour permettre au front d'envoyer au back des parameters à la recherche de données externes, on définit une liste de parameters
	qui seront routés du front vers l'API. Par mesure de sécurité, on ne les forwarde pas tous.

 ```sql   
INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_DONNEES_EXTERNES_PARAMETER_LIST', 'Http parameters renvoyés par le front dans l''appel aux données externes', '["numerocontrat","numerofacture","numerotiers","demandeId"]');
```
### Parameters spécifiques au TS - LOGDOMC
```sql
INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_ULIS_UPLOAD', 'logdomc: envoyer les données vers Ulis à la validation de demande', 'false');

INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_ULIS_DOWNLOAD', 'logdomc: recevoir les données depuis Ulis à l''initialisation d''une demande', 'false');
```

# Resid usager v2

## Fonction de surlignage de données dans le recap

Au cours de l'implémentation de RESCART un besoin a été soulevé pour souligner en gras des champs dans le recap BO d'une demande.

Afin d'en faire bénéficier tous les téléservices une méthode à Overrider à été ajouté dans le service DemarchesDataProvider. 

Cette méthode, getSpansIdAMarquer(DemandeDTO demande) retourne une liste de String, ces String étant l'id du span à souligner. Un exemple d'implémentation adapté à RESCART est décrit ci dessous

```java
@Override
	public List<String> getSpansIdAMarquer(DemandeDTO demande) {
		List<String> result = new ArrayList<>();
		if(rescartUtils.isRenouvellementOuChangement(demande)) {
			result.add("donneeIdentiteNom");
			result.add("donneeIdentitePrenoms");
			result.add("donneeIdentiteNomusage");
			result.add("donneeIdentiteSexe");
			result.add("donneeIdentiteDatenaissance");
			result.add("donneeIdentiteLieunaissanceville");
			result.add("donneeIdentitePaysnaissance");
			result.add("donneeIdentiteNationalite");
			result.add("donneeIdentiteTypedocument");
			result.add("donneeIdentiteCin");
			result.add("donneeIdentiteDatedevalidite");
			result.add("donneeIdentitePaysdelivrancepiece");
			result.add("donneeIdentiteAutrenationalite");
			result.add("donneeIdentiteDatedelivraison");
		}
		return result;
	}
```

Si cette fonctionnalitée n'est pas utile dans un autre TS une méthode par défaut a été implémenté dans l'interface retournant une liste vide.
