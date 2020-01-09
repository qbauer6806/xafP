#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
-- #16457 - Supprimer la limite de caractère en base de données du champs texte du justificatif et/ou du courrier envoyé à l'usager.
ALTER TABLE ${artifactIdLower}.act_hi_detail ALTER COLUMN text_ TYPE text;