-- #12951 - Stocker en base la date de création d'un fichier d'une demande
ALTER TABLE ${artifactIdUpper}.DEM_DEMANDES_FILES ADD COLUMN DATE timestamp without time zone;
