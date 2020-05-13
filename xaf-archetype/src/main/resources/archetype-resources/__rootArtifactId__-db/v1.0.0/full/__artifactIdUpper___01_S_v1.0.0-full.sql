#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
-- 1) Important pour ne pas avoir des caractères mal encodés à l'affichage.
set client_encoding = 'utf8';
-- 2) Création du schéma
drop schema ${artifactIdUpper} cascade;
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



CREATE SEQUENCE dem_demandes_pk_demandes_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes (
    pk_demandes integer DEFAULT nextval('dem_demandes_pk_demandes_seq'),
    agent_affecte_id character varying(128),
    canal character varying(30) NOT NULL,
    contenu text NOT NULL,
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




CREATE SEQUENCE dem_demandes_complements_files_pk_demandescomplementsfiles_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_complements_files (
    pk_demandescomplementsfiles integer DEFAULT nextval('dem_demandes_complements_files_pk_demandescomplementsfiles_seq'),
    meta character varying(512),
    name character varying(1024) NOT NULL,
    url character varying(1024) NOT NULL,
    fk_demandescomplements integer
);




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




CREATE SEQUENCE dem_demandes_data_pk_demandesdata_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_data (
    pk_demandesdata integer DEFAULT nextval('dem_demandes_data_pk_demandesdata_seq'),
    key character varying(256) NOT NULL,
    value character varying(10000),
    fk_demandes integer
);




CREATE SEQUENCE dem_demandes_files_pk_demandesfiles_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_files (
    pk_demandesfiles integer DEFAULT nextval('dem_demandes_files_pk_demandesfiles_seq'),
    meta character varying(512),
    name character varying(1024) NOT NULL,
    url character varying(1024) NOT NULL,
    fk_demandes integer
);




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




CREATE SEQUENCE dem_demandes_statuts_pk_demandesstatuts_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_demandes_statuts (
    pk_demandesstatuts integer DEFAULT nextval('dem_demandes_statuts_pk_demandesstatuts_seq'),
    agent_id character varying(128),
    code_motif character varying(128),
    commentaire character varying(8000),
    texte_a_envoyer text,
    date timestamp without time zone NOT NULL,
    libelle character varying(64) NOT NULL,
    usager_id integer,
    fk_demandes integer
);




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




CREATE SEQUENCE dem_motifs_pk_motifs_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_motifs (
    pk_motifs integer DEFAULT nextval('dem_motifs_pk_motifs_seq'),
	fk_demarcheid character varying(128) NOT NULL,
	libelle character varying(256) NOT NULL,
    code character varying(128) NOT NULL,
    statut character varying(64) NOT NULL,
	langue character varying(2) NOT NULL,
    date_archive timestamp without time zone,
    commentaire_prerempli character varying(2048),
    texte_a_envoyer character varying(2048)
);




CREATE SEQUENCE dem_periodes_ouverture_pk_periodesouverture_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_periodes_ouverture (
    pk_periodesouverture integer DEFAULT nextval('dem_periodes_ouverture_pk_periodesouverture_seq'),
    date_debut timestamp without time zone NOT NULL,
    date_fin timestamp without time zone NOT NULL,
    fk_demarcheid character varying(128) NOT NULL
);




CREATE SEQUENCE dem_templates_pk_templates_seq START 1;

CREATE TABLE ${artifactIdUpper}.dem_templates (
    pk_templates integer DEFAULT nextval('dem_templates_pk_templates_seq'),
    fk_demarcheid character varying(128) NOT NULL,
    code character varying(256) NOT NULL,
    contenu character varying(10000) NOT NULL,
    langue character varying(2)
);




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

-- #10315 - Permettre de générer l'identifiant de la demande à partir d'un code propre à chaque démarche
ALTER TABLE ${artifactIdUpper}.DEM_DEMARCHES ADD COLUMN IDENTIFIANT_PREFIXE CHARACTER VARYING(128) NOT NULL;

-- #10127 - [BO] Formulaire création usager courrier - modification champ obligatoire + ajout tooltip
ALTER TABLE ${artifactIdUpper}.DEM_USAGERS_COURRIER ALTER COLUMN NOM DROP NOT NULL;
-- #12285 - Garder une trace du Prénom, Nom et Email de l'usager qui a fait la demande
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_NOM CHARACTER VARYING(256);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_PRENOM CHARACTER VARYING(256);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN USAGER_EMAIL CHARACTER VARYING(256);

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

-- #12951 - Stocker en base le buildId et le recapType
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN BUILD_ID CHARACTER VARYING(32);
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES ADD COLUMN RECAP_TYPE CHARACTER VARYING(256);

-- #12951 - Stocker en base la date de création d'un fichier d'une demande
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES_FILES ADD COLUMN DATE timestamp without time zone;

-- #16457 - Supprimer la limite de caractère en base de données du champs texte du justificatif et/ou du courrier envoyé à l'usager.
ALTER TABLE ${artifactIdLower}.act_hi_detail ALTER COLUMN text_ TYPE text;

-- #18993 - Ajouter une table de configuration de propriétés spécifiques à la démarche.
CREATE SEQUENCE ${artifactIdLower}.dem_properties_seq START 1;

CREATE TABLE ${artifactIdLower}.dem_properties (
    pk_properties INTEGER DEFAULT nextval('detsala.dem_properties_seq'),
    fk_demarcheid CHARACTER VARYING(128) NOT NULL,
    type CHARACTER VARYING(256) NOT NULL,
    key CHARACTER VARYING(256) NOT NULL,
    value CHARACTER VARYING(10000),
    UNIQUE(key)
);

ALTER TABLE ONLY ${artifactIdLower}.dem_properties
    ADD CONSTRAINT dem_properties_pkey PRIMARY KEY (pk_properties);

-- Fin scripts de "Structure"

-- 5) Ajout des grant : Important ! Donner les droits d'utilisation au user applicatif et tous les priviliège au user admin.
grant usage on schema ${artifactIdUpper} to ${artifactIdUpper};
grant all on schema ${artifactIdUpper} to ${artifactIdUpper}_admin;
grant all privileges on all tables in schema ${artifactIdUpper} to ${artifactIdUpper};
grant all privileges on all sequences in schema ${artifactIdUpper} to ${artifactIdUpper};