-- #10315 - Permettre de générer l'identifiant de la demande à partir d'un code propre à chaque démarche
ALTER TABLE ${artifactIdUpper}.DEM_DEMARCHES ADD COLUMN IDENTIFIANT_PREFIXE CHARACTER VARYING(128) NOT NULL;
