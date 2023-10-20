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
