XAF 11.1.0 - LOGDOMC Changes

1) Fonctionnalité de modification d'une demande dans un état "EN ATTENTE DE TRAITEMENT"

Impact:
	Code: XAF
	DB:
		ALTER TABLE logdomc.dem_demandes ADD COLUMN modification_timestamp bigint;

2) Fonctionnalité liées au DLNUF - Utilisation d'une interface commune pour les données externes, enregistrement des données externes en DB pour comparaison fututre

Impact
	Code: XAF
	DB:
		ALTER TABLE logdomc.dem_demandes ADD COLUMN contenuInitial character varying(10000);
		ALTER TABLE logdomc.dem_brouillons ADD COLUMN contenuInitial character varying(10000);

Parameters liés aux données externes
	Pour permettre au front d'envoyer au back des parameters à la recherche de données externes, on définit une liste de parameters
	qui seront routés du front vers l'API. Par mesure de sécurité, on ne les forwarde pas tous.
	INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_DONNEES_EXTERNES_PARAMETER_LIST', 'Http parameters renvoyés par le front dans l''appel aux données externes', '["numerocontrat","numerofacture","numerotiers","demandeId"]');

Parameters spécifiques au TS - LOGDOMC
INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_ULIS_UPLOAD', 'logdomc: envoyer les données vers Ulis à la validation de demande', 'false');

INSERT INTO LOGDOMC.DEM_PROPERTIES (fk_demarcheid, type, key, descriptif, value) VALUES ('LOGDOMC', 'FRONT_AF', 'XAF_ULIS_DOWNLOAD', 'logdomc: recevoir les données depuis Ulis à l''initialisation d''une demande', 'false');
