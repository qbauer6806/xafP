package mc.gouv.af.back.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.json.simple.parser.ParseException;

import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.logon.apiclient.RestException;

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une
 * demande.
 * 
 * @author qdeme
 * @author mboutelier.ext
 *
 */
public interface DemandeRecapHTMLService {

	/**
	 * Méthode générant la partie d'informations de la demande de la page.
	 * 
	 * @param demande
	 * @return
	 */
	String getHTMLDemandeGeneric(DemandeDTO demande);
	
	String getHTMLDemandeComplements(DemandeDTO demande) throws RestException;

	/**
	 * Méthode générant la partie demande initiale de la page, elle est générée en
	 * utilisant le fichier JSON recap implémenté par le WISYWIG
	 * 
	 * @param demande
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	String getHTMLDemandeContenuRecap(DemandeDTO demande)
			throws IOException, ParseException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

}
