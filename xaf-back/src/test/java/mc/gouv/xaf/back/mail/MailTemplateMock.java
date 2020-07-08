package mc.gouv.xaf.back.mail;

public class MailTemplateMock {

    public static final String acceptSubjectFRCode = "123";
    public static final String accepteContentFRCode = "456";

    public static final String acceptSubjectFR = "Accusé de réception de votre candidature ${identifiant}' ";

    public static final String accepteContentFR = "Bonjour ${titre} ${nom} ${prenom},"
            + "<br/><br/>Nous avons bien reçu votre candidature ${identifiant}.<br/><br/>Concernant le demande ${pkDemande} <br/><br/>Nous"
            + " vous précisons que les recrutements de personnel auxquels procède l’Administration monégasque "
            + "font l’objet d’avis de vacances d’emploi qui indiquent la nature et les caractéristiques des postes à"
            + " pourvoir, ainsi que les conditions à remplir par les candidats. Ces avis sont publiés in extenso au "
            + "Journal de Monaco, bulletin officiel hebdomadaire de la Principauté paraissant le vendredi, "
            + "ainsi que sur le site du Gouvernement <a href=\"${urlFront}\">www.gouv.mc</a> <br/><br/>Il"
            + " vous appartiendra donc, lorsque vous aurez connaissance, par la publicité qui en est ainsi faite, "
            + "de vacances d’emplois susceptibles de vous convenir, de faire acte de candidature dans la mesure où "
            + "vous rempliriez les conditions requises.<br/><br/>Votre candidature en ligne sera conservée pendant "
            + "un an.Pour le motif ${motif}. Au-delà de ce délai, vous pourrez déposer une nouvelle candidature "
            + "spontanée aux emplois de "
            + "la fonction publique d''État en contactant ${utilisateur} concerant l'usager : ${usager} "
            + "sur le site <a href=\"${urlBack}\">www.gouv.mc</a>"
            + "<br/><br/>Cordialement,<br/><br/>Direction des Ressources Humaines et de "
            + "la Formation de la Fonction Publique<br/>3ème étage - Stade Louis II - Entrée H<br/>1, Avenue des"
            + " Castelans<br/>BP 672<br/>MC 98014 MONACO CEDEX<br/><br/>Ce message a été envoyé automatiquement. "
            + "Nous vous remercions de ne pas y répondre.<br/>Si vous n''êtes pas à l''origine de cette demande, "
            + "veuillez simplement ignorer ce message. <br/><br/> Commentaire : ${commentaire}.<br/>";

    public static final String acceptSubjectEN = "Acknowledgment of receipt your application ${identifiant}' ";

    public static final String accepteContentEN = "hello dear ${titre} ${nom} ${prenom},"
            + "<br/><br/>We have received your application for ${identifiant}.<br/><br/>"
            + "Concerning the vacancy ${pkDemande} <br/><br/>we"
            + " have to precise than the recrutments  are proceed by Monaco's Administration law's "
            + " More information has published on  Monaco newspapers you can find this information on "
            + " official Monaco government's website <a href=\"${urlFront}\">www.gouv.mc</a> <br/><br/>"
            + " It will give you the information about new vacancies available in Monaco public services"
            + " which are compatible with your skills and your knowledge "
            + " <br/><br/> Your on line application with be keep for one year. Based on ${motif}."
            + " you can find more details by contacting : ${utilisateur} mentioning : ${usager}"
            + " on the website <a href=\"${urlBack}\">www.gouv.mc</a>"
            + "<br/><br/>Sincerely <br/><br/>Direction des Ressources Humaines et de "
            + "la Formation de la Fonction Publique<br/>3ème étage - Stade Louis II - Entrée H<br/>1, Avenue des"
            + " Castelans<br/>BP 672<br/>MC 98014 MONACO CEDEX<br/><br/>"
            + " PS : ${commentaire}.<br/>";
}
