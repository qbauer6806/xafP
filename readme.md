
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

# Backlog #47828

Dans la méthode {TsCode}ApiServiceImpl.creerDemande dans le catch en cas d'exception il faut ajouter à la méthode de suppression un booléen indiquant si un brouillon est présent ou non. S'il l'est (booléen à true) alors les fichiers rattaché à ce brouillon ne seront pas supprimé dans file.