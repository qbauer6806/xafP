#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
-- 1) Important pour ne pas avoir des caractères mal encodés à l'affichage.
set client_encoding = 'utf8';
-- 2) Création du schéma
drop schema if exists ${artifactIdUpper} cascade;
create schema ${artifactIdUpper};
-- 3) Paramètrage du schéma
set search_path = ${artifactIdUpper}, pg_catalog;
set default_with_oids = false;
-- La table space par défaut. A voir avec les DBA, ensuite on décidéra de décommenter la ligne suivante ou pas.
-- set default_tablespace = ''; 

-- 4) Début scripts de "Structure" : create table, alter, etc....
create table ${artifactIdLower}.ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
);

insert into ${artifactIdLower}.ACT_GE_PROPERTY
values ('schema.version', '5.22.0.0', 1);

insert into ${artifactIdLower}.ACT_GE_PROPERTY
values ('schema.history', 'create(5.22.0.0)', 1);

insert into ${artifactIdLower}.ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

create table ${artifactIdLower}.ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ bytea,
    GENERATED_ boolean,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp,
    LAST_UPDATE_TIME_ timestamp,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ boolean,
    IS_CONCURRENT_ boolean,
    IS_SCOPE_ boolean,
    IS_EVENT_SCOPE_ boolean,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    LOCK_TIME_ timestamp,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RE_PROCDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DESCRIPTION_ varchar(4000),
    HAS_START_FORM_KEY_ boolean,
    HAS_GRAPHICAL_NOTATION_ boolean,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    DUE_DATE_ timestamp,
    CATEGORY_ varchar(255),
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    FORM_KEY_ varchar(255),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar (64),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp not null,
    PROC_DEF_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_EVT_LOG (
    LOG_NR_ SERIAL PRIMARY KEY,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ bytea,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp null,
    IS_PROCESSED_ smallint default 0
);

create table ${artifactIdLower}.ACT_PROCDEF_INFO (
	ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    REV_ integer,
    INFO_JSON_ID_ varchar(64),
    primary key (ID_)
);

create index ACT_IDX_EXEC_BUSKEY on ${artifactIdLower}.ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_TASK_CREATE on ${artifactIdLower}.ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on ${artifactIdLower}.ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ${artifactIdLower}.ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ${artifactIdLower}.ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_VARIABLE_TASK_ID on ${artifactIdLower}.ACT_RU_VARIABLE(TASK_ID_);

create index ACT_IDX_BYTEAR_DEPL on ${artifactIdLower}.ACT_GE_BYTEARRAY(DEPLOYMENT_ID_);
alter table ${artifactIdLower}.ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
    foreign key (DEPLOYMENT_ID_)
    references ${artifactIdLower}.ACT_RE_DEPLOYMENT (ID_);

alter table ${artifactIdLower}.ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);

create index ACT_IDX_EXE_PROCINST on ${artifactIdLower}.ACT_RU_EXECUTION(PROC_INST_ID_);
alter table ${artifactIdLower}.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST
    foreign key (PROC_INST_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PARENT on ${artifactIdLower}.ACT_RU_EXECUTION(PARENT_ID_);
alter table ${artifactIdLower}.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT
    foreign key (PARENT_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_SUPER on ${artifactIdLower}.ACT_RU_EXECUTION(SUPER_EXEC_);
alter table ${artifactIdLower}.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER
    foreign key (SUPER_EXEC_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PROCDEF on ${artifactIdLower}.ACT_RU_EXECUTION(PROC_DEF_ID_);
alter table ${artifactIdLower}.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ${artifactIdLower}.ACT_RE_PROCDEF (ID_);

create index ACT_IDX_TSKASS_TASK on ${artifactIdLower}.ACT_RU_IDENTITYLINK(TASK_ID_);
alter table ${artifactIdLower}.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
    foreign key (TASK_ID_)
    references ${artifactIdLower}.ACT_RU_TASK (ID_);

create index ACT_IDX_ATHRZ_PROCEDEF on ${artifactIdLower}.ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
alter table ${artifactIdLower}.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_)
    references ${artifactIdLower}.ACT_RE_PROCDEF (ID_);

create index ACT_IDX_IDL_PROCINST on ${artifactIdLower}.ACT_RU_IDENTITYLINK(PROC_INST_ID_);
alter table ${artifactIdLower}.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_EXEC on ${artifactIdLower}.ACT_RU_TASK(EXECUTION_ID_);
alter table ${artifactIdLower}.ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_PROCINST on ${artifactIdLower}.ACT_RU_TASK(PROC_INST_ID_);
alter table ${artifactIdLower}.ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_TASK_PROCDEF on ${artifactIdLower}.ACT_RU_TASK(PROC_DEF_ID_);
alter table ${artifactIdLower}.ACT_RU_TASK
  	add constraint ACT_FK_TASK_PROCDEF
  	foreign key (PROC_DEF_ID_)
  	references ${artifactIdLower}.ACT_RE_PROCDEF (ID_);

create index ACT_IDX_VAR_EXE on ${artifactIdLower}.ACT_RU_VARIABLE(EXECUTION_ID_);
alter table ${artifactIdLower}.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_EXE
    foreign key (EXECUTION_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_VAR_PROCINST on ${artifactIdLower}.ACT_RU_VARIABLE(PROC_INST_ID_);
alter table ${artifactIdLower}.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION(ID_);

create index ACT_IDX_VAR_BYTEARRAY on ${artifactIdLower}.ACT_RU_VARIABLE(BYTEARRAY_ID_);
alter table ${artifactIdLower}.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_BYTEARRAY
    foreign key (BYTEARRAY_ID_)
    references ${artifactIdLower}.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_JOB_EXCEPTION on ${artifactIdLower}.ACT_RU_JOB(EXCEPTION_STACK_ID_);
alter table ${artifactIdLower}.ACT_RU_JOB
    add constraint ACT_FK_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_)
    references ${artifactIdLower}.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_EVENT_SUBSCR on ${artifactIdLower}.ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
alter table ${artifactIdLower}.ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references ${artifactIdLower}.ACT_RU_EXECUTION(ID_);

create index ACT_IDX_MODEL_SOURCE on ${artifactIdLower}.ACT_RE_MODEL(EDITOR_SOURCE_VALUE_ID_);
alter table ${artifactIdLower}.ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE
    foreign key (EDITOR_SOURCE_VALUE_ID_)
    references ${artifactIdLower}.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_SOURCE_EXTRA on ${artifactIdLower}.ACT_RE_MODEL(EDITOR_SOURCE_EXTRA_VALUE_ID_);
alter table ${artifactIdLower}.ACT_RE_MODEL
    add constraint ACT_FK_MODEL_SOURCE_EXTRA
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_)
    references ${artifactIdLower}.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_DEPLOYMENT on ${artifactIdLower}.ACT_RE_MODEL(DEPLOYMENT_ID_);
alter table ${artifactIdLower}.ACT_RE_MODEL
    add constraint ACT_FK_MODEL_DEPLOYMENT
    foreign key (DEPLOYMENT_ID_)
    references ${artifactIdLower}.ACT_RE_DEPLOYMENT (ID_);

create index ACT_IDX_PROCDEF_INFO_JSON on ${artifactIdLower}.ACT_PROCDEF_INFO(INFO_JSON_ID_);
alter table ${artifactIdLower}.ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_JSON_BA
    foreign key (INFO_JSON_ID_)
    references ${artifactIdLower}.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_PROCDEF_INFO_PROC on ${artifactIdLower}.ACT_PROCDEF_INFO(PROC_DEF_ID_);
alter table ${artifactIdLower}.ACT_PROCDEF_INFO
    add constraint ACT_FK_INFO_PROCDEF
    foreign key (PROC_DEF_ID_)
    references ${artifactIdLower}.ACT_RE_PROCDEF (ID_);

alter table ${artifactIdLower}.ACT_PROCDEF_INFO
    add constraint ACT_UNIQ_INFO_PROCDEF
    unique (PROC_DEF_ID_);

create table ${artifactIdLower}.ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

create table ${artifactIdLower}.ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    CLAIM_TIME_ timestamp,
    END_TIME_ timestamp,
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    PRIORITY_ integer,
    DUE_DATE_ timestamp,
    FORM_KEY_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    CREATE_TIME_ timestamp,
    LAST_UPDATED_TIME_ timestamp,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(64),
    REV_ integer,
    TIME_ timestamp not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TYPE_ varchar(255),
    TIME_ timestamp not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTION_ varchar(255),
    MESSAGE_ varchar(4000),
    FULL_MSG_ bytea,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(4000),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(4000),
    CONTENT_ID_ varchar(64),
    TIME_ timestamp,
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    primary key (ID_)
);


create index ACT_IDX_HI_PRO_INST_END on ${artifactIdLower}.ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on ${artifactIdLower}.ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on ${artifactIdLower}.ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on ${artifactIdLower}.ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on ${artifactIdLower}.ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on ${artifactIdLower}.ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on ${artifactIdLower}.ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on ${artifactIdLower}.ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on ${artifactIdLower}.ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on ${artifactIdLower}.ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on ${artifactIdLower}.ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_PROCVAR_TASK_ID on ${artifactIdLower}.ACT_HI_VARINST(TASK_ID_);
create index ACT_IDX_HI_ACT_INST_PROCINST on ${artifactIdLower}.ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on ${artifactIdLower}.ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_IDENT_LNK_USER on ${artifactIdLower}.ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on ${artifactIdLower}.ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on ${artifactIdLower}.ACT_HI_IDENTITYLINK(PROC_INST_ID_);
create index ACT_IDX_HI_TASK_INST_PROCINST on ${artifactIdLower}.ACT_HI_TASKINST(PROC_INST_ID_);

create table ${artifactIdLower}.ACT_ID_GROUP (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(64),
    GROUP_ID_ varchar(64),
    primary key (USER_ID_, GROUP_ID_)
);

create table ${artifactIdLower}.ACT_ID_USER (
    ID_ varchar(64),
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    PICTURE_ID_ varchar(64),
    primary key (ID_)
);

create table ${artifactIdLower}.ACT_ID_INFO (
    ID_ varchar(64),
    REV_ integer,
    USER_ID_ varchar(64),
    TYPE_ varchar(64),
    KEY_ varchar(255),
    VALUE_ varchar(255),
    PASSWORD_ bytea,
    PARENT_ID_ varchar(255),
    primary key (ID_)
);

create index ACT_IDX_MEMB_GROUP on ${artifactIdLower}.ACT_ID_MEMBERSHIP(GROUP_ID_);
alter table ${artifactIdLower}.ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_GROUP
    foreign key (GROUP_ID_)
    references ${artifactIdLower}.ACT_ID_GROUP (ID_);

create index ACT_IDX_MEMB_USER on ${artifactIdLower}.ACT_ID_MEMBERSHIP(USER_ID_);
alter table ${artifactIdLower}.ACT_ID_MEMBERSHIP
    add constraint ACT_FK_MEMB_USER
    foreign key (USER_ID_)
    references ${artifactIdLower}.ACT_ID_USER (ID_);


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

ALTER TABLE ${artifactIdUpper}.DEM_USAGERS_COURRIER ALTER COLUMN NOM DROP NOT NULL;

ALTER TABLE ${artifactIdUpper}.DEM_DEMARCHES ADD COLUMN IDENTIFIANT_PREFIXE CHARACTER VARYING(128) NOT NULL;

ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_NOM CHARACTER VARYING(256);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_PRENOM CHARACTER VARYING(256);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_EMAIL CHARACTER VARYING(256);

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

ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN BUILD_ID CHARACTER VARYING(32);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN RECAP_TYPE CHARACTER VARYING(256);

ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES_FILES ADD COLUMN DATE timestamp without time zone;

-- Fin scripts de "Structure"

-- 5) Ajout des grant : Important ! Donner les droits d'utilisation au user applicatif et tous les priviliège au user admin.
grant usage on schema ${artifactIdUpper} to ${artifactIdUpper};
grant all on schema ${artifactIdUpper} to ${artifactIdUpper}_admin;
grant all privileges on all tables in schema ${artifactIdUpper} to ${artifactIdUpper};
grant all privileges on all sequences in schema ${artifactIdUpper} to ${artifactIdUpper};

-- 6) Début scripts de "Data" : Insert, Update, Delete

--Requête générées depuis la moulinette à partir des données du front
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Le demandeur', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Informations de l''entreprise', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('La dérogation', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.demandeur.titre', 'Titre', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Le demandeur'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.demandeur.prenom', 'Prénom du demandeur', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Le demandeur'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.demandeur.nom', 'Nom du demandeur', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Le demandeur'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.demandeur.mail', 'Email de contact', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Le demandeur'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.numerocar', 'N° CAR', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.raisonsociale', 'Raison Sociale', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.nom', 'Nom de l''entreprise', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.ligne1', 'Adresse ligne 1', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.ligne2', 'Adresse ligne 2', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.ligne3', 'Adresse ligne 3', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.codePostal', 'Code postal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.ville', 'Ville', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresse.pays', 'Pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.telephone', 'Téléphone de l''entreprise', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations de l''entreprise'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.derogation.joursferies', 'Jours fériés légaux', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'La dérogation'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.derogation.employe.concerne', 'Personnel concerné par la dérogation', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'La dérogation'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.derogation.motivationdemande', 'Motivation de la demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'La dérogation'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.derogation.presencedeleguespersonnel', 'Avez-vous des délégués du personnel?', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'La dérogation'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.derogation.nombresalarie', 'Nombre de salariés dans l''entreprise', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'La dérogation'), 'false');
--Configuration par défaut
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Demande', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Canal', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Informations réservées à l''administration', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Dernier statut', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Usager', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Pièce(s) jointe(s)', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Complément de demande', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Fichiers internes', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Historique des statuts', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Données', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Agent', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Courrier', 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'access.demarcheId', 'Identifiant de la démarche', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'access.usagerId', 'Identifiant de l''usager', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'access.fkAccess', '', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'agentAffecteNomAffichage', 'Nom d''afichage de l''agent responsable', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.paysCode', 'Code du pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.etat', 'Etat', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.dateDerConnexion', 'Date de la dernière connexion', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.dateActivation', 'Date d''activation', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.dateCreation', 'Date de création', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.paysId', 'Id du pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'usager.id', 'Identifiant de l''usager', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'canal.code', 'Code du canal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Canal'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'agentAffecteId', 'Identifiant de l''agent', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dernierStatut.codeMotif', 'Code du motif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dernierStatut.pkStatut', 'Id du statut', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dernierStatut.code', 'Code du statut de la demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'pkDemandes', 'Identifiant de la demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'creeParAgentId', 'Identifiant de l''agent qui a crée la demande', null, 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'statuts.code', 'Code du statut de la demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'statuts.codeMotif', 'Code du motif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dateCreation', 'Date de création', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dateDerModif', 'Date de dernière modification', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'courrierDateReception', 'Date de réception', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Détail des informations personnelles de l’usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'data.demandeId', 'Id de la demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Données'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'data.pkDemandesData', 'Id de la donnée', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Données'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'statuts.pkStatut', 'Id du statut', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dernierStatut.agentId', 'Id agent', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'dernierStatut.usagerId', 'Id usager', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'access.active', 'Actif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'statuts.agentId', 'Id agent', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'statuts.usagerId', 'Id usager', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichiers.demandeId', 'Id demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichiers.url', 'Url', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichiers.language', 'Langue', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichiers.id', 'Identifiant', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichiers.type', 'Type de fichier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'complement.fichiers.demandeId', 'Id demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'complement.fichiers.url', 'Url', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'complement.fichiers.language', 'Langue', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'complement.fichiers.id', 'Identifiant', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'complement.fichiers.type', 'Type de fichier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichierinterne.fichiers.demandeId', 'Id demande', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichierinterne.fichiers.url', 'Url', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichierinterne.fichiers.language', 'Langue', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichierinterne.fichiers.id', 'Identifiant', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('false', 'fichierinterne.fichiers.type', 'Type de fichier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'canal.libelle', 'Mode de réception', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Canal'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'langue', 'Langue', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'observations', 'Observations', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations réservées à l''administration'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'dernierStatut.libelle', 'Libellé', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'dernierStatut.date', 'Date', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'dernierStatut.commentaire', 'Commentaire', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'dernierStatut.libelleMotif', 'Libellé du motif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Dernier statut'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'statuts.date', 'Date du statut', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'statuts.commentaire', 'Commentaire', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'statuts.libelleMotif', 'Libellé du motif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'statuts.libelle', 'Libellé du statut', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Historique des statuts'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'identifiant', 'Identifiant', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'fichiers.content', 'Contenu', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'fichiers.name', 'Nom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Pièce(s) jointe(s)'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'complement.fichiers.content', 'Contenu', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'complement.fichiers.name', 'Nom de la pièce jointe', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'complements.statut', 'Statut', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'complements.question.codeMotif', 'Code du motif', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Complément de demande'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'data.key', 'Clé', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Données'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'data.value', 'Valeur', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Données'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.nom', 'Nom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.prenom', 'Prénom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.adresse1', 'Adresse 1', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.adresse2', 'Adresse 2', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.codePostal', 'Code postal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.ville', 'Ville', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.nomPays', 'Pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.complementAdresse', 'Complément d''adresse', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.raisonSociale', 'Raison sociale', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.email', 'Email', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.login', 'Identifiant du compte usager', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'usager.titre', 'Titre', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Usager'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.matricule', 'Matricule', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.nom', 'Nom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.nomUsage', 'Nom d''usage', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.nomNaissance', 'Nom de naissance', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.prenom', 'Prénom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'agent.mail', 'Email', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Agent'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'courrierRefInterne', 'Référence interne', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Courrier'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'nomsCourriers', 'Nom', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Courrier'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'fichierinterne.fichiers.content', 'Contenu', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'fichierinterne.fichiers.name', 'Nom du fichier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Fichiers internes'), 'true');

-- /!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!${symbol_escape} ATTENTION REMPLACER L'ADRESSE DU SERVICE PAR UNE ADRESSE DE TEST, SI ENVIRONNEMENT != PROD /!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!${symbol_escape}
-- Penser également à mettre email_from = email_service afin d'éviter que le secrétariat de la DAN (contact@gouv.mc) ne reçoive des e-mails de delivery failure
-- Pour DEVC + REC
INSERT INTO ${artifactIdUpper}.DEM_DEMARCHES VALUES ('${artifactIdUpper}','${tsFullName}','recettedae10@gouv.mc','Le Service de l’Inspection du Travail','noreply@gouv.mc','No-Reply','recettedae10@gouv.mc','Contact Téléservices Principauté de Monaco','DJF');

-- PROD (à dé-commenter lors de la MEP)
-- INSERT INTO ${artifactIdUpper}.DEM_DEMARCHES VALUES ('${artifactIdUpper}','${tsFullName}','inspectiondutravail@gouv.mc','Le Service de l’Inspection du Travail','noreply@gouv.mc','No-Reply','contact@gouv.mc','Contact Téléservices Principauté de Monaco','DJF');

-- MOTIFS - FR
------------------------------------------
-- Motifs - INFOS COMP
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','L’avis des délégués du personnel est manquant','AVIS_DELEGUES_MANQUANT','EN_ATTENTE_COMPL','fr',null,
                                       'Afin de pouvoir traiter votre demande, je vous serais très obligée de bien vouloir me faire parvenir dans les meilleurs délais l’avis des délégués du personnel.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','L’avis du personnel concerné est manquant ','AVIS_PERSONNEL_MANQUANT','EN_ATTENTE_COMPL','fr',null,
                                       'Afin de pouvoir traiter votre demande, je vous serais très obligée de bien vouloir me faire parvenir dans les meilleurs délais l’avis du personnel concerné.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Des informations complémentaires sont nécessaires','INFOS_COMP_NECESSAIRES','EN_ATTENTE_COMPL','fr',null,
                                       null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','ATTENTE_COMPL_AUTRE','EN_ATTENTE_COMPL','fr',null,
                                       null);

-- Motifs - REFUSEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Avis défavorable des délégués du personnel','AVIS_DELEGUES_DEFAVORABLE','REFUSEE','fr',null,
                                       'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison de l’avis défavorable émis par les délégués du personnel.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Avis défavorable des salariés concernés','AVIS_SALARIE_DEFAVORABLE','REFUSEE','fr',null,
                                       'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison de l’avis défavorable émis par le personnel concerné.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Demande hors délai','DEMANDE_HORS_DELAIS','REFUSEE','fr',null,
                                       E'Me référant à votre communication, je suis au regret de vous informer qu’eu égard à la réception tardive de votre demande, il n’a pas été possible de réserver une suite favorable à votre requête.${symbol_escape}nA l’avenir, vous voudrez bien me faire parvenir vos demandes dans des délais me permettant de les instruire.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Refusée (Jours fériés particuliers) + accordée (Jours fériés)','AVIS_PERSONNEL_DEFAVORABLE','REFUSEE','fr',null,'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList} En ce qui concerne le(s) jour(s) suivant(s) ${symbol_dollar}{joursFeriesExceptionnels}, j’ai le regret de vous informer qu’il n’est pas d’usage d’accorder de dérogation pour ce(s) jour(s), eu égard à leur importance et à leur caractère symbolique.');
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','REFUSEE_AUTRE','REFUSEE','fr',null,'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison « commentaire libre ».');

-- Motifs - ACCORDEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés)','ACCORDEE','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés + jours fériés particuliers)','ACCORDEE_ET_ACCORDEE_EXCEPTIONNEL','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}De même, j’ai l’honneur de vous informer que s’agissant du/des jours suivant(s) ${symbol_dollar}{joursFeriesExceptionnels}, une dérogation vous est accordée à titre exceptionnel, au regard de la nature de votre activité.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés particuliers)','ACCORDEE_EXCEPTIONNEL','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et à titre exceptionnel au regard de la nature de votre activité, la dérogation demandée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesExceptionnelsList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés + chantier)','ACCORDEE_CHANTIER','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et des dispositions de l''Arrêté ministériel n. 2018-1116 du 3 décembre 2018 relatif à l''encadrement des chantiers, la dérogation sollicitée vous est accordée pour le personnel visé le(s): ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée aux seules personnes ayant données leur accord','ACCORDEE_PERSONNES_ACCORD','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation demandée vous est accordée afin de pouvoir occuper les salariés qui ont émis un avis favorable, le(s) : ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée nonobstant l''avis défavorable des délégués du personnel','ACCORDEE_AVIS_DELEGUE_DEFAVORABLE','ACCORDEE','fr',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et nonobstant l’avis défavorable des délégués du personnel, la dérogation demandée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}');

-- Motifs - ANNULEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l''entreprise','ANNULATION_PAR_ENTREPRISE','ANNULEE','fr',null,null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l''agent','ANNULATION_PAR_AGENT','ANNULEE','fr',null,null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Désinscription','ANNULATION_DESINSCRIPTION','ANNULEE','fr',null,null);


-- MOTIFS - EN
------------------------------------------
-- Motifs - INFOS COMP
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','L’avis des délégués du personnel est manquant','AVIS_DELEGUES_MANQUANT','EN_ATTENTE_COMPL','en',null,
                                       'Afin de pouvoir traiter votre demande, je vous serais très obligée de bien vouloir me faire parvenir dans les meilleurs délais l’avis des délégués du personnel.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','L’avis du personnel concerné est manquant ','AVIS_PERSONNEL_MANQUANT','EN_ATTENTE_COMPL','en',null,
                                       'Afin de pouvoir traiter votre demande, je vous serais très obligée de bien vouloir me faire parvenir dans les meilleurs délais l’avis du personnel concerné.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Des informations complémentaires sont nécessaires','INFOS_COMP_NECESSAIRES','EN_ATTENTE_COMPL','en',null,
                                       null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','ATTENTE_COMPL_AUTRE','EN_ATTENTE_COMPL','en',null,
                                       null);

-- Motifs - REFUSEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Avis défavorable des délégués du personnel','AVIS_DELEGUES_DEFAVORABLE','REFUSEE','en',null,
                                       'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison de l’avis défavorable émis par les délégués du personnel.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Avis défavorable des salariés concernés','AVIS_SALARIE_DEFAVORABLE','REFUSEE','en',null,
                                       'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison de l’avis défavorable émis par le personnel concerné.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Demande hors délai','DEMANDE_HORS_DELAIS','REFUSEE','en',null,
                                       E'Me référant à votre communication, je suis au regret de vous informer qu’eu égard à la réception tardive de votre demande, il n’a pas été possible de réserver une suite favorable à votre requête.${symbol_escape}nA l’avenir, vous voudrez bien me faire parvenir vos demandes dans des délais me permettant de les instruire.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Refusée (Jours fériés particuliers) + accordée (Jours fériés)','AVIS_PERSONNEL_DEFAVORABLE','REFUSEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList} En ce qui concerne le(s) jour(s) suivant(s) ${symbol_dollar}{joursFeriesExceptionnels}, j’ai le regret de vous informer qu’il n’est pas d’usage d’accorder de dérogation pour ce(s) jour(s), eu égard à leur importance et à leur caractère symbolique.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','REFUSEE_AUTRE','REFUSEE','en',null,
                                       'Me référant à votre demande, j’ai le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux ne peut vous être accordée en raison « commentaire libre ».');

-- Motifs - ACCORDEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés)','ACCORDEE','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés + jours fériés particuliers)','ACCORDEE_ET_ACCORDEE_EXCEPTIONNEL','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation sollicitée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}De même, j’ai l’honneur de vous informer que s’agissant du/des jours suivant(s) ${symbol_dollar}{joursFeriesExceptionnels}, une dérogation vous est accordée à titre exceptionnel, au regard de la nature de votre activité.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés particuliers)','ACCORDEE_EXCEPTIONNEL','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et à titre exceptionnel au regard de la nature de votre activité, la dérogation demandée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesExceptionnelsList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée (Jours fériés + chantier)','ACCORDEE_CHANTIER','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et des dispositions de l''Arrêté ministériel n. 2018-1116 du 3 décembre 2018 relatif à l''encadrement des chantiers, la dérogation sollicitée vous est accordée pour le personnel visé le(s): ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée aux seules personnes ayant données leur accord','ACCORDEE_PERSONNES_ACCORD','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail, la dérogation demandée vous est accordée afin de pouvoir occuper les salariés qui ont émis un avis favorable, le(s) : ${symbol_dollar}{joursFeriesList}');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Accordée nonobstant l''avis défavorable des délégués du personnel','ACCORDEE_AVIS_DELEGUE_DEFAVORABLE','ACCORDEE','en',null,
                                       'Me référant à votre demande, j’ai l’honneur de vous informer que, sous réserve du respect des dispositions de l’article 7 de la Loi n. 800 du 18 février 1966 concernant la récupération ou la rémunération de cette/ces journée(s) de travail et nonobstant l’avis défavorable des délégués du personnel, la dérogation demandée vous est accordée afin de pouvoir occuper le personnel de votre établissement, le(s) : ${symbol_dollar}{joursFeriesList}');

-- Motifs - ANNULEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l''entreprise','ANNULATION_PAR_ENTREPRISE','ANNULEE','en',null,null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l''agent','ANNULATION_PAR_AGENT','ANNULEE','en',null,null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Désinscription','ANNULATION_DESINSCRIPTION','ANNULEE','en',null,null);


-- MAILS - FR
------------------------------------------

-- Mails Usagers
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_OBJET', 'Accusé de réception de votre demande ${symbol_dollar}{identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_CORPS', 'Madame, Monsieur,<br/><br/>Nous avons bien reçu votre demande de dérogation relative aux jours fériés légaux n°<a href="${symbol_dollar}{urlFront}demande_view.html?id=${symbol_dollar}{pkDemande}">${symbol_dollar}{identifiant}</a>.<br/><br/>Vous pouvez consulter votre demande en cliquant sur <a href="${symbol_dollar}{urlFront}">ce lien</a>.<br/><br/>Nous allons traiter votre demande.<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_OBJET', 'Compléter votre demande ${symbol_dollar}{identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_CORPS', 'Madame, Monsieur,<br/><br/>Afin de pouvoir traiter votre demande n° ${symbol_dollar}{identifiant}, nous vous serions très obligés de bien vouloir nous faire parvenir dans les meilleurs délais la/les pièce(s) justificative(s) manquante(s).<br/><br/>Pour compléter votre demande par la démarche en ligne, veuillez-vous connecter au téléservice « Demander une dérogation relative aux jours fériés légaux ».<br/><br/>Pour vous connecter au téléservice cliquer sur <a href="${symbol_dollar}{urlFront}">ce lien</a>.<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_ACCORDEE_OBJET', 'Votre demande ${symbol_dollar}{identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_ACCORDEE_CORPS', 'Madame, Monsieur,<br/><br/>Le Service de l’Inspection du Travail a le plaisir de vous informer que votre demande de dérogation relative aux jours fériés légaux n° ${symbol_dollar}{identifiant} est accordée.<br/><br/>Vous pouvez consulter votre demande et télécharger votre justificatif de dérogation relative aux jours fériés légaux, en ligne, sur le téléservice « Demander une dérogation relative aux jours fériés légaux ».<br/><br/>Pour vous connecter au téléservice cliquer sur <a href="${symbol_dollar}{urlFront}">ce lien</a>.<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_OBJET', 'Votre demande ${symbol_dollar}{identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_CORPS', 'Madame, Monsieur,<br/><br/>Le Service de l’Inspection du Travail a bien pris en compte l’annulation de votre demande de dérogation relative aux jours fériés légaux n° ${symbol_dollar}{identifiant}.<br/><br/>Vous pouvez consulter votre demande, en ligne, sur le téléservice « Demander une dérogation relative aux jours fériés légaux ».<br/><br/>Pour vous connecter au téléservice cliquer sur <a href="${symbol_dollar}{urlFront}">ce lien</a>.<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_OBJET', 'Votre demande ${symbol_dollar}{identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_CORPS', 'Madame, Monsieur,<br/><br/>Le Service de l’Inspection du Travail a le regret de vous informer que votre demande de dérogation relative aux jours fériés légaux n° ${symbol_dollar}{identifiant} est refusée.<br/><br/>Vous pouvez consulter votre demande et télécharger votre justificatif de refus, en ligne, sur le téléservice « Demander une dérogation relative aux jours fériés légaux ».<br/><br/>Pour vous connecter au téléservice cliquer sur <a href="${symbol_dollar}{urlFront}">ce lien</a>.<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET', 'Désinscription du téléservice "Demander une dérogation relative aux jours fériés légaux"', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS', 'Madame, Monsieur,<br/><br/>Nous vous confirmons votre désinscription du téléservice "Demander une dérogation relative aux jours fériés légaux" sur votre compte Téléservices Principauté de Monaco pour l''identifiant ${symbol_dollar}{identifiant_usager} (voir les <a href="https://teleservice.gouv.mc/${tsFrontUrl}/legalterms.html">Conditions Générales d''Utilisation</a>).<br/><br/>Cordialement.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

-- Mails Agents
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_OBJET', 'La demande ${symbol_dollar}{identifiant} a été complétée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>La demande ${symbol_dollar}{identifiant} a été complétée. Vous pouvez la consulter <a href="${symbol_dollar}{urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_OBJET', 'La demande ${symbol_dollar}{identifiant} a été complétée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_CORPS', 'Bonjour,<br/><br/>La demande ${symbol_dollar}{identifiant} a été complétée par ${symbol_dollar}{utilisateur}. Vous pouvez la consulter <a href="${symbol_dollar}{urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ATTENTE_VALIDATION_OBJET', 'La demande ${symbol_dollar}{identifiant} est en attente de validation hiérarchique', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ATTENTE_VALIDATION_CORPS', 'Bonjour,<br/><br/>La demande ${symbol_dollar}{identifiant} vient d’être refusée et nécessite votre validation hiérarchique. Vous pouvez la consulter <a href="${symbol_dollar}{urlBack}">à cette adresse</a>.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_OBJET', 'La demande ${symbol_dollar}{identifiant} a été annulée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${symbol_dollar}{usager} vient d''annuler sa demande ${symbol_dollar}{identifiant}.<br/><br/>Vous pouvez la consulter à cette <a href="${symbol_dollar}{urlBack}/demandes/${symbol_dollar}{pkDemande}">adresse</a>.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_OBJET', 'Désinscription de l''usager ${symbol_dollar}{usager}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${symbol_dollar}{usager} vient de se désinscrire de la démarche.<br/><br/>Par conséquent, les demandes suivantes sont passées à l''état "Annulée" :<br/>${symbol_dollar}{demandes}<br/><br/>Notification automatique', 'fr');

-- MAILS - EN
------------------------------------------

-- Mails Usagers
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_OBJET', 'Acknowledgement of receipt of your application  ${symbol_dollar}{identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_CORPS', 'Madam, Sir,<br/><br/>We have received your application for an exemption to statutory public holidays no. <a href="${symbol_dollar}{urlFront}demande_view.html?id=${symbol_dollar}{pkDemande}">${symbol_dollar}{identifiant}</a>.<br/><br/>You can consult your request by clicking <a href="${symbol_dollar}{urlFront}">on this link</a><br/><br/>We will now process your application.<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_OBJET', 'Complete your application ${symbol_dollar}{identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_CORPS', 'Madam, Sir,<br/><br/>To enable us to process your application no. ${symbol_dollar}{identifiant}, we would be grateful if you could send us the missing supporting document(s) as soon as possible.<br/><br/>Please log in to the online service: “Requesting an exemption to statutory public holidays” in order to complete your application online.<br/><br/>To connect to the online service, click <a href="${symbol_dollar}{urlFront}">on this link</a>.<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_ACCORDEE_OBJET', 'Your application ${symbol_dollar}{identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_ACCORDEE_CORPS', 'Madam, Sir,<br/><br/>The Labour Inspectorate is pleased to inform you that your application for an exemption to statutory public holidays no. ${symbol_dollar}{identifiant} has been successful.<br/><br/>You can view your application and download your exemption confirmation online using the online service: “Requesting an exemption to statutory public holidays”.<br/><br/>To connect to the online service, click <a href="${symbol_dollar}{urlFront}">on this link</a>.<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_OBJET', 'Your application ${symbol_dollar}{identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_CORPS', 'Madam, Sir,<br/><br/>The Labour Inspectorate regrets to inform you that your application for an exemption to statutory public holidays no. ${symbol_dollar}{identifiant} has been rejected.<br/><br/>You can view your application and download your rejection notice online using the online service: “Requesting an exemption to statutory public holidays”.<br/><br/>To connect to the online service, click <a href="${symbol_dollar}{urlFront}">on this link</a>.<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_OBJET', 'Your application ${symbol_dollar}{identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_CORPS', 'Madam, Sir,<br/><br/>The Labour Inspectorate has noted the cancellation of your application for an exemption to statutory public holidays no. ${symbol_dollar}{identifiant}.<br/><br/>You can view your application online using the online service: “Requesting an exemption to statutory public holidays”.<br/><br/>To connect to the online service, click <a href="${symbol_dollar}{urlFront}">on this link</a>.<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET', 'Unsubscribe from online service: “Requesting an exemption to statutory public holidays”', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS', 'Madam, Sir,<br/><br/>We confirm that you have unsubscribed from the online service: “Requesting an exemption to statutory public holidays” on your account with the Principality of Monaco''s online services for the identifier ${symbol_dollar}{identifiant_usager} (see the General Terms of Use, which are available on <a href="https://teleservice.gouv.mc/${tsFrontUrl}/legalterms.html">this page </a>).<br/><br/>Kind regards.<br/><br/>Service de l’Inspection du Travail<br/>La Frégate<br/>2, rue Princesse Antoinette<br/>98000 Monaco<br/>(+377) 98 98 87 26<br/>inspectiondutravail@gouv.mc<br/><br/>This message has been sent automatically. Please do not reply to it.<br/>If you did not send this request, please simply ignore this message.', 'en');