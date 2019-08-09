-- #10127 - [BO] Formulaire création usager courrier - modification champ obligatoire + ajout tooltip
ALTER TABLE ${artifactIdUpper}.DEM_USAGERS_COURRIER ALTER COLUMN NOM DROP NOT NULL;
