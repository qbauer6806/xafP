#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
-- Début scripts de "Data" : Insert, Update, Delete

-- /!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\ ATTENTION REMPLACER L'ADRESSE DU SERVICE PAR UNE ADRESSE DE TEST, SI ENVIRONNEMENT != PROD /!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\
-- Penser également à mettre email_from = email_service afin d'éviter que le secrétariat de la DAN (contact@gouv.mc) ne reçoive des e-mails de delivery failure
INSERT INTO ${artifactIdUpper}.DEM_DEMARCHES VALUES ('${artifactIdUpper}','${tsFullName}','recettedae10@gouv.mc','Service de l''Emploi','noreply@gouv.mc','No-Reply','contact@gouv.mc','Contact Téléservices Principauté de Monaco','${tsIdentifiantDemande}');

-- MOTIFS - FR
------------------------------------------
-- Motifs - VALIDEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default, '${artifactIdUpper}','Validation totale chantier', 'VALIDATION_TOT_CHANTIER', 'VALIDEE', 'fr', null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nsur le chantier ${nomChantier} en Principauté de Monaco, est validée.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}', 'Validation totale hors chantier','VALIDATION_TOT_HORS_CHANTIER', 'VALIDEE','fr',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nau sein de ${nomEntreprise} en Principauté de Monaco, est validée.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default, '${artifactIdUpper}','Validation partielle chantier','VALIDATION_PARTIELLE_CHANTIER','VALIDEE','fr',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nsur le chantier ${nomChantier} en Principauté de Monaco, est validée.\n\n[INSÉRER ICI LA LISTE DES SALARIÉS NON AUTORISÉS À INTERVENIR]');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Validation partielle hors chantier','VALIDATION_PARTIELLE_HORS_CHANTIER','VALIDEE','fr',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nau sein de ${nomEntreprise} en Principauté de Monaco, est validée.\n\n[INSÉRER ICI LA LISTE DES SALARIÉS NON AUTORISÉS À INTERVENIR]');

-- Motifs - INFOS COMP
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Pièces jointes illisibles','PIECES_ILLISIBLES','EN_ATTENTE_COMPL','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Pièces justificatives manquantes','PIECES_MANQUANTE','EN_ATTENTE_COMPL','fr', null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','ATTENTE_COMPL_AUTRE','EN_ATTENTE_COMPL','fr', null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

-- Motifs - REFUSEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – FR – Hors chantier','DEMANDE_SUP_3_MOIS_FR_HC','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation de détachement suivant la convention Franco-Monégasque de sécurité sociale du 28 février 1952, (certificat d’assujettissement – formulaire SE 138/01) à retirer et à faire valider par la Caisse Primaire d''Assurance Maladie, dont dépendent vos salariés,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – FR - Chantier','DEMANDE_SUP_3_FR_MOIS','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation délivrée par la Direction de l’Expansion Economique vous autorisant à intervenir sur ce chantier,
-l’autorisation de détachement suivant la convention Franco-Monégasque de sécurité sociale du 28 février 1952, (certificat d’assujettissement – formulaire SE 138/01) à retirer et à faire valider par la Caisse Primaire d''Assurance Maladie, dont dépendent vos salariés,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – IT - Chantier','DEMANDE_SUP_3_IT_MOIS','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation délivrée par la Direction de l’Expansion Economique vous autorisant à intervenir sur ce chantier,
-les formulaires M/I/C1 ou M/I/C2 dûment complétés et validés par l’INPS ou l’INAIL, pour ce chantier,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – IT - Hors chantier','DEMANDE_SUP_3_MOIS_IT_HC','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-les formulaires M/I/C1 ou M/I/C2 dûment complétés et validés par l’INPS ou l’INAIL
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Nationalité italienne requise','NATIONALITE_IT_REQUISE','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'Seul le personnel de nationalité italienne peut faire l’objet d’un détachement, conformément aux termes de la convention italo-monégasque de sécurité sociale.\n\nAussi je vous confirme que votre (vos) salarié(s) n’est (ne sont) donc pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Demande en double','DEMANDE_DOUBLE','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce détachement a déjà fait l’objet d’une demande, traitée par le Service de l’Emploi.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Détachement non autorisé','DET_NON_AUTORISE','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Conformément à l’autorisation délivrée par la Direction de l’Expansion Economique, aucun détachement de personnel ne peut être accepté. Il vous appartient de déclarer l’(les) intéressé(s) auprès des organismes sociaux monégasques.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','REFUSEE_AUTRE','REFUSEE','fr',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

-- Motifs - ANNULEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l’entreprise','ANNULATION_PAR_ENTREPRISE','ANNULEE','fr',null,
                                       'Nous avons bien pris en compte l’annulation de votre demande.',null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l’agent','ANNULATION_PAR_AGENT','ANNULEE','fr',null,
                                       'Votre demande d''annulation a bien été prise en compte.',null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Désinscription','ANNULATION_DESINSCRIPTION','ANNULEE','fr',null,null,null);


-- MOTIFS - EN
------------------------------------------
-- Motifs - VALIDEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default, '${artifactIdUpper}','Validation totale chantier', 'VALIDATION_TOT_CHANTIER', 'VALIDEE', 'en', null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nsur le chantier ${nomChantier} en Principauté de Monaco, est validée.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}', 'Validation totale hors chantier','VALIDATION_TOT_HORS_CHANTIER', 'VALIDEE','en',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nau sein de ${nomEntreprise} en Principauté de Monaco, est validée.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default, '${artifactIdUpper}','Validation partielle chantier','VALIDATION_PARTIELLE_CHANTIER','VALIDEE','en',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nsur le chantier ${nomChantier} en Principauté de Monaco, est validée.\n\n[INSÉRER ICI LA LISTE DES SALARIÉS NON AUTORISÉS À INTERVENIR]');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Validation partielle hors chantier','VALIDATION_PARTIELLE_HORS_CHANTIER','VALIDEE','en',null,
                                       E'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'J''ai l''honneur de vous informer que votre demande ${identifiant}, relative au détachement du(des) salarié(s) suivant(s) : ${salariesADetacher}\nau sein de ${nomEntreprise} en Principauté de Monaco, est validée.\n\n[INSÉRER ICI LA LISTE DES SALARIÉS NON AUTORISÉS À INTERVENIR]');

-- Motifs - INFOS COMP
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Pièces jointes illisibles','PIECES_ILLISIBLES','EN_ATTENTE_COMPL','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Pièces justificatives manquantes','PIECES_MANQUANTE','EN_ATTENTE_COMPL','en', null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','ATTENTE_COMPL_AUTRE','EN_ATTENTE_COMPL','en', null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

-- Motifs - REFUSEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – FR – Hors chantier','DEMANDE_SUP_3_MOIS_FR_HC','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation de détachement suivant la convention Franco-Monégasque de sécurité sociale du 28 février 1952, (certificat d’assujettissement – formulaire SE 138/01) à retirer et à faire valider par la Caisse Primaire d''Assurance Maladie, dont dépendent vos salariés,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – FR - Chantier','DEMANDE_SUP_3_FR_MOIS','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation délivrée par la Direction de l’Expansion Economique vous autorisant à intervenir sur ce chantier,
-l’autorisation de détachement suivant la convention Franco-Monégasque de sécurité sociale du 28 février 1952, (certificat d’assujettissement – formulaire SE 138/01) à retirer et à faire valider par la Caisse Primaire d''Assurance Maladie, dont dépendent vos salariés,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – IT - Chantier','DEMANDE_SUP_3_IT_MOIS','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-les formulaires M/I/C1 ou M/I/C2 dûment complétés et validés par l’INPS ou l’INAIL
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Durée du détachement supérieure à 3 mois – IT - Hors chantier','DEMANDE_SUP_3_MOIS_IT_HC','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce téléservice ne s’adressant qu’aux demandes de détachement inférieur à trois mois, il convient de vous rapprocher du Service de l’Emploi afin de transmettre le dossier sous format papier, qui devra être constitué de :
-l’autorisation délivrée par la Direction de l’Expansion Economique vous autorisant à intervenir sur ce chantier,
-les formulaires M/I/C1 ou M/I/C2 dûment complétés et validés par l’INPS ou l’INAIL, pour ce chantier,
-la copie de leur pièce d’identité, recto et verso,
-leur adresse d’hébergement dans la région durant leur intervention.

Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Nationalité italienne requise','NATIONALITE_IT_REQUISE','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       E'Seul le personnel de nationalité italienne peut faire l’objet d’un détachement, conformément aux termes de la convention italo-monégasque de sécurité sociale.\n\nAussi je vous confirme que votre (vos) salarié(s) n’est (ne sont) donc pas autorisé(s) à intervenir en Principauté.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Demande en double','DEMANDE_DOUBLE','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Ce détachement a déjà fait l’objet d’une demande, traitée par le Service de l’Emploi.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Détachement non autorisé','DET_NON_AUTORISE','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.',
                                       'Conformément à l’autorisation délivrée par la Direction de l’Expansion Economique, aucun détachement de personnel ne peut être accepté. Il vous appartient de déclarer l’(les) intéressé(s) auprès des organismes sociaux monégasques.');

insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Autre','REFUSEE_AUTRE','REFUSEE','en',null,
                                       'Veuillez prendre connaissance du document émis par le Service de l’Emploi, téléchargeable ci-après.', null);

-- Motifs - ANNULEE
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l’entreprise','ANNULATION_PAR_ENTREPRISE','ANNULEE','en',null,
                                       'Nous avons bien pris en compte l’annulation de votre demande.',null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Annulation par l’agent','ANNULATION_PAR_AGENT','ANNULEE','en',null,
                                       'Votre demande d''annulation a bien été prise en compte.',null);
insert into ${artifactIdUpper}.DEM_MOTIFS values (default,'${artifactIdUpper}','Désinscription','ANNULATION_DESINSCRIPTION','ANNULEE','en',null,null,null);

-- MAILS - FR
------------------------------------------
-- Mails Usagers
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_OBJET', 'Accusé de réception de votre demande ${identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_CORPS', 'Bonjour,<br/><br/>J''ai l''honneur d''accuser réception de votre transmission <a href="${urlFront}demande_view.html?id=${pkDemande}">${identifiant}</a>. Nous allons étudier votre demande.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_OBJET', 'Compléter votre demande ${identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_CORPS', 'Bonjour,<br/><br/>Afin de pouvoir traiter votre demande ${identifiant}, nous avons besoin d’informations complémentaires.<br/><br/>Pour compléter votre demande via la démarche en ligne, veuillez cliquer sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_VALIDEE_OBJET', 'Votre demande ${identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_VALIDEE_CORPS', 'Bonjour,<br/><br/>Nous avons le plaisir de vous informer que votre demande <a href="${urlFront}demande_view.html?id=${pkDemande}">${identifiant}</a> est traitée.<br/><br/>Vous pouvez consulter votre demande et télécharger l’autorisation de détachement en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_OBJET', 'Votre demande ${identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_CORPS', 'Bonjour,<br/><br/>Nous avons bien pris en compte l’annulation de votre demande ${identifiant}.<br/><br/>Vous pouvez consulter votre demande en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a><br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_OBJET', 'Votre demande ${identifiant}', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_CORPS', 'Bonjour,<br/><br/>Nous avons le regret de vous informer que votre demande ${identifiant} ne peut obtenir une suite favorable.<br/><br/>Vous pouvez consulter votre demande en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET', 'Désinscription du téléservice "${tsFullName}"', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS', 'Bonjour,<br/><br/>Nous vous confirmons votre désinscription du téléservice "${tsFullName}" sur votre compte Téléservices Principauté de Monaco pour l''identifiant ${identifiant_usager} (voir les <a href="https://teleservice.gouv.mc/${tsFrontUrl}/legalterms.html">Conditions Générales d''Utilisation</a>).<br/><br/>Par conséquent, nous vous informons que toutes vos demandes en cours ont été annulées.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'fr');

-- Mails Agents
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_OBJET', 'La demande ${identifiant} a été complétée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>La demande ${identifiant} a été complétée. Vous pouvez la consulter <a href="${urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_OBJET', 'La demande ${identifiant} a été complétée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_CORPS', 'Bonjour,<br/><br/>La demande ${identifiant} a été complétée par ${utilisateur}. Vous pouvez la consulter <a href="${urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_OBJET', 'La demande ${identifiant} a été annulée', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${usager} vient d''annuler sa demande ${identifiant}.<br/><br/>Vous pouvez la consulter <a href="${urlBack}/demandes/${pkDemande}">à cette adresse</a>.<br/><br/>Notification automatique', 'fr');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_OBJET', 'Désinscription de l''usager ${usager} du téléservice "${tsFullName}"', 'fr');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${usager} vient de se désinscrire de la démarche.<br/><br/>Par conséquent, les demandes suivantes sont passées à l''état "Annulée" :<br/>${demandes}<br/><br/>Notification automatique', 'fr');


-- MAILS - EN
------------------------------------------
-- Mails Usagers
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_OBJET', 'Accusé de réception de votre demande ${identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CREATION_DEMANDE_USAGER_CORPS', 'Bonjour,<br/><br/>J''ai l''honneur d''accuser réception de votre transmission <a href="${urlFront}demande_view.html?id=${pkDemande}">${identifiant}</a>. Nous allons étudier votre demande.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_OBJET', 'Compléter votre demande ${identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_DEMANDEIC_CORPS', 'Bonjour,<br/><br/>Afin de pouvoir traiter votre demande ${identifiant}, nous avons besoin d’informations complémentaires.<br/><br/>Pour compléter votre demande via la démarche en ligne, veuillez cliquer sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Je vous rappelle qu’à ce jour et jusqu’à régularisation de votre dossier, votre (vos) salarié(s) n’est (ne sont) pas autorisé(s) à intervenir en Principauté.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_VALIDEE_OBJET', 'Votre demande ${identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_VALIDEE_CORPS', 'Bonjour,<br/><br/>Nous avons le plaisir de vous informer que votre demande <a href="${urlFront}demande_view.html?id=${pkDemande}">${identifiant}</a> est traitée.<br/><br/>Vous pouvez consulter votre demande et télécharger l’autorisation de détachement en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_OBJET', 'Votre demande ${identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_CONFIRMATION_ACTION_ANNULER_CORPS', 'Bonjour,<br/><br/>Nous avons bien pris en compte l’annulation de votre demande ${identifiant}.<br/><br/>Vous pouvez consulter votre demande en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a><br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_OBJET', 'Votre demande ${identifiant}', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_ACTION_REFUSER_CORPS', 'Bonjour,<br/><br/>Nous avons le regret de vous informer que votre demande ${identifiant} ne peut obtenir une suite favorable.<br/><br/>Vous pouvez consulter votre demande en cliquant sur ce <a href="${urlFront}demande_view.html?id=${pkDemande}">lien</a>.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_OBJET', 'Désinscription du téléservice "${tsFullName}"', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_POUR_USAGER_CORPS', 'Bonjour,<br/><br/>Nous vous confirmons votre désinscription du téléservice "${tsFullName}" sur votre compte Téléservices Principauté de Monaco pour l''identifiant ${identifiant_usager} (voir les <a href="https://teleservice.gouv.mc/${tsFrontUrl}/legalterms.html">Conditions Générales d''Utilisation</a>).<br/><br/>Par conséquent, nous vous informons que toutes vos demandes en cours ont été annulées.<br/><br/>Cordialement,<br/><br/>Service de l''Emploi<br/>La Frégate<br/>2 rue Princesse Antoinette<br/>MC 98000 MONACO<br/><br/>(+377) 98 98 88 14<br/><br/><b>Ce message a été envoyé automatiquement. Nous vous remercions de ne pas y répondre.</b><br/>Si vous n''êtes pas à l''origine de cette demande, veuillez simplement ignorer ce message.', 'en');

-- Mails Agents
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_OBJET', 'La demande ${identifiant} a été complétée', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>La demande ${identifiant} a été complétée. Vous pouvez la consulter <a href="${urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_OBJET', 'La demande ${identifiant} a été complétée', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_REPONSE_IC_PAR_AGENT_CORPS', 'Bonjour,<br/><br/>La demande ${identifiant} a été complétée par ${utilisateur}. Vous pouvez la consulter <a href="${urlBack}">à cette adresse</a> dans le tableau des demandes en cours de traitement.<br/><br/>Notification automatique', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_OBJET', 'La demande ${identifiant} a été annulée', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_NOTIFICATION_ANNULATION_DEMANDE_PAR_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${usager} vient d''annuler sa demande ${identifiant}.<br/><br/>Vous pouvez la consulter <a href="${urlBack}/demandes/${pkDemande}">à cette adresse</a>.<br/><br/>Notification automatique', 'en');

insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_OBJET', 'Désinscription de l''usager ${usager} du téléservice "${tsFullName}"', 'en');
insert into ${artifactIdUpper}.DEM_TEMPLATES values (default, '${artifactIdUpper}', 'MAIL_DESINSCRIPTION_USAGER_CORPS', 'Bonjour,<br/><br/>L''usager ${usager} vient de se désinscrire de la démarche.<br/><br/>Par conséquent, les demandes suivantes sont passées à l''état "Annulée" :<br/>${demandes}<br/><br/>Notification automatique', 'en');

--Requête générées depuis la moulinette à partir des données du front
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Informations sur le demandeur', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Informations sur l''entreprise d''origine', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Informations sur le détachement', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Salarié(s) à détacher', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_cat_config (libelle, editable) VALUES ('Pièces justificatives', 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.raisonsociale', 'Raison sociale', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.ligne1', 'Adresse ligne 1', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.ligne2', 'Adresse ligne 2', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.ligne3', 'Adresse ligne 3', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.codePostal', 'Code postal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.ville', 'Ville', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entrepriseorigine.adresse.pays', 'Pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur l''entreprise d''origine'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.paysoriginedetachement', 'Pays d''origine du détachement', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.datesdetachement', 'Dates de détachement', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.estchantier', 'Intervenez-vous dans le cadre d''un chantier ?', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.nomchantier', 'Nom du chantier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.nomentreprise', 'Nom de l''entreprise d''accueil', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.nautorisationchantier', 'Numéro d''autorisation', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.datedebutchantier', 'Date de début du chantier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.datefinchantier', 'Date de fin du chantier', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.ligne1', 'Adresse ligne 1', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.ligne2', 'Adresse ligne 2', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.ligne3', 'Adresse ligne 3', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.codePostal', 'Code postal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.ville', 'Ville', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adressechantier.pays', 'Pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.ligne1', 'Adresse ligne 1', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.ligne2', 'Adresse ligne 2', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.ligne3', 'Adresse ligne 3', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.codePostal', 'Code postal', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.ville', 'Ville', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
INSERT INTO ${artifactIdLower}.dem_recherche_champ_config (enabled, cle, libelle, fk_categorie, editable) VALUES ('true', 'contenu.donnee.entreprise.adresseentreprise.pays', 'Pays', (select id from ${artifactIdLower}.dem_recherche_cat_config where libelle = 'Informations sur le détachement'), 'false');
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

-- Fin scripts "Data"