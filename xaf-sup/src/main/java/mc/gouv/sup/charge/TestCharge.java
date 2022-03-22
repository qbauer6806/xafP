package mc.gouv.sup.charge;

import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xapi.error.exception.server.InternalErrorWebException;

/**
 * 
 * Classe permettant d'effectuer un test de charge sur l'API d'un téléservice
 * Exemple d'exécution du jar xaf-sup pour cette classe :
 * 		java -cp xaf-sup-9.1.0-SNAPSHOT.jar mc.gouv.sup.charge.TestCharge -c C:\qdeme\testcharge.conf -nd 1 -r -i 200-500 -nf 2-5
 * 
 * @author qdeme
 *
 */
public class TestCharge {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCharge.class);
	
	private static Map<String, String> conf = new HashMap<String, String>();
	
	private static List<String> contenus = new ArrayList<String>();
	
	private static List<DemandeFileDTO> fichiers = new ArrayList<DemandeFileDTO>();
	
	private static Map<String, DemandeFileDTO> fichiersDejaUtilisesPourDoublons = new HashMap<String, DemandeFileDTO>();
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static AfApiClient apiClient;
	
	private static final String CONFIG_APIURL = "api.url";
	private static final String CONFIG_APIJWT = "api.jwt";
	private static final String CONFIG_CANAUX = "canaux";
	private static final String CONFIG_LANGUES = "langues";
	private static final String CONFIG_BUILDID = "buildid";
	private static final String CONFIG_USAGERIDS = "usagerids";

	public static void main(String[] args) throws ParseException, AWTException, JsonMappingException, JsonProcessingException {
		
        Options options = new Options();

        Option optionConf = new Option("c", "conf", true, "fichier de configuration");
        optionConf.setRequired(true);
        options.addOption(optionConf);
        
        Option optionNombreDemandes = new Option("nd", "nombredemandes", true, "nombre de demandes");
        optionNombreDemandes.setRequired(true);
        options.addOption(optionNombreDemandes);
        
        Option optionNombreFichiers = new Option("nf", "nombrefichiers", true, "nombre de fichiers (syntaxe n1-n2 ou 0)");
        optionNombreFichiers.setRequired(true);
        options.addOption(optionNombreFichiers);

        Option optionIntervalle = new Option("i", "intervalle", true, "intervalle entre chaque demande (ms) (syntaxe i1-i2 si -r spécifié)");
        optionIntervalle.setRequired(true);
        options.addOption(optionIntervalle);

        Option optionRandom = new Option("r", "random", false, "random (aléatoire) (implique -i i1-i2 (ms))");
        optionRandom.setRequired(false);
        options.addOption(optionRandom);
        
        Option optionSansDoublons = new Option("s", "sansdoublons", false, "sans doublons de fichiers (implique un jeu de données d'au moins nd*nf(n2) fichiers)");
        optionSansDoublons.setRequired(false);
        options.addOption(optionSansDoublons);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose 

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            LOGGER.error("Erreur lors du parsing des arguments", e);
            formatter.printHelp("TestCharge", options);

            System.exit(1);
		}
        
        try {
			loadConf(cmd.getOptionValue("conf"));
		} catch (IOException e) {
			LOGGER.error("Erreur lors de la lecture du fichier de configuration", e);
		}
        
        apiClient = new AfApiClient(conf.get(CONFIG_APIURL), conf.get(CONFIG_APIJWT));

        LOGGER.info("====== Analyse du scénario...");
        Integer nombre = Integer.parseInt(cmd.getOptionValue("nombredemandes"));
        boolean random = cmd.hasOption("random");
        
        String intervalleStr = cmd.getOptionValue("intervalle");
        Integer intervalle = null;
        Integer intervalle1 = null;
        Integer intervalle2 = null;
        if (random) {
        	String[] intervalles = intervalleStr.split("-");
        	if (intervalles.length < 2) {
        		LOGGER.error("Format d'intervalles non valide. Si option -r activée, alors -i doit avoir le format i1-i2 (ms)");
        		System.exit(1);
        	}
        	intervalle1 = Integer.parseInt(intervalles[0]);
        	intervalle2 = Integer.parseInt(intervalles[1]);
        }
        else {
        	intervalle = Integer.parseInt(intervalleStr);
        }
        
        boolean sansDoublons = cmd.hasOption("sansdoublons");
        String nombrefichiers = cmd.getOptionValue("nombrefichiers");
        
        String messageSansDoublons = "";
        String messageFichiers = null;
        
        
        if (sansDoublons && !"0".equals(nombrefichiers)) {
        	String spl[] = nombrefichiers.split("-");
        	if (Integer.parseInt(spl[1])*nombre > fichiers.size()) {
	        	LOGGER.error("Erreur, le produit du nombre de demandes par le nombre maximal de fichiers par demandes dépasse le nombre de fichiers dans le jeu de données !");
	        	System.exit(1);
        	}
        }
        
        if (sansDoublons) {
        	messageSansDoublons = ", sans doublons";
        }
        
        if ("0".equals(nombrefichiers)) {
        	messageFichiers = "Les demandes n'auront pas de fichiers joints.";
        }
        else {
        	String spl[] = nombrefichiers.split("-");
        	messageFichiers = "Chaque demande comportera entre " + spl[0] + " et " + spl[1] + " fichiers joints" + messageSansDoublons + ".";
        }
        if (!random) {
        	LOGGER.info("Créer {} demandes, création toutes les {} ms. {}", nombre, intervalle, messageFichiers);
        }
        else {
        	LOGGER.info("Créer {} demandes, création toutes les {} à {} ms. {}", nombre, intervalle1, intervalle2, messageFichiers);
        }
        LOGGER.info("======\n");
		
        LOGGER.info("====== Lancement des threads...");
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < nombre; i++) {
			try {
				if (!random) {
					Thread.sleep(intervalle);
				}
				else {
					Random r = new Random();
					Integer duration = r.nextInt((intervalle2 - intervalle1) + 1) + intervalle1;
					Thread.sleep(duration);
				}
			} catch (InterruptedException e) {
				LOGGER.error("Erreur lors du Thread.sleep()", e);
				Thread.currentThread().interrupt();
			}
			
			ExecutorService executorService = Executors.newFixedThreadPool(10);
			Future<?> future = executorService.submit(() -> creerDemande(nombrefichiers, sansDoublons));
			futures.add(future);
		}
		LOGGER.info("======\n");
		
		LOGGER.info("====== En attente de la terminaison des threads...");
		boolean allDone = false;
		while (!allDone) {
			boolean finished = true;
			for (Future<?> future : futures) {
				if (!future.isDone()) {
					finished = false;
				}
			}
			allDone = finished;
		}
		LOGGER.info("======\n");
		
		LOGGER.info("Tous les threads sont terminés");
		
		System.exit(0);
		
	}
	
	private static void creerDemande(String nombrefichiers, boolean sansDoublons) {
		
		DemandeInputDTO input = new DemandeInputDTO();
		input.setBuildId(conf.get(CONFIG_BUILDID));
		input.setCanal(DemandeCanalEnum.valueOf(getRandomConfElement(CONFIG_CANAUX)));
		input.setLangue(getRandomConfElement(CONFIG_LANGUES));
		input.setRecapType("projectDemandeRecap");
		
		if (!"0".equals(nombrefichiers)) {
			String spl[] = nombrefichiers.split("-");
			Integer n1 = Integer.parseInt(spl[0]);
			Integer n2 = Integer.parseInt(spl[1]);
			Integer nombredefinitif = ThreadLocalRandom.current().nextInt((n2 - n1) + 1) + n1;
			List<DemandeFileDTO> tmpFichiers = null;
			if (sansDoublons) {
				tmpFichiers = getFichiersSansDoublons(nombredefinitif);
			}
			else {
				tmpFichiers = getFichiersAvecDoublons(nombredefinitif);
			}
			input.setFichiers(tmpFichiers.stream().toArray(DemandeFileDTO[]::new));
		}
		
		JsonNode contenu = null;
		try {
			contenu = mapper.readValue(getRandomContenu(), JsonNode.class);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors du passage String->JsonNode du contenu", e);
		}
		input.setContenu(contenu);
		
		Integer usagerId = Integer.parseInt(getRandomConfElement(CONFIG_USAGERIDS));
		
		LOGGER.info("Création demande... (usagerid={}, canal={}, langue={}, fichiers={})", usagerId, input.getCanal().name(), input.getLangue(), input.getFichiers().length);
		
		try {
//			for (DemandeFileDTO f : input.getFichiers()) {
//				System.out.println(f.getUrl());
//			}
			DemandeDTO demande = apiClient.creerDemande(input, usagerId);
			LOGGER.info("Demande créée : {}", demande.getPkDemandes());
		}
		catch (Exception e) {
			if (e instanceof InternalErrorWebException) {
				LOGGER.error("Erreur lors de la création de la demande (httpStatus={})", ((InternalErrorWebException) e).getHttpStatus());
			}
			else {
				LOGGER.error("Erreur lors de la création de la demande", e);
			}
		}
	}
	
	private static List<DemandeFileDTO> getFichiersAvecDoublons(Integer nombre) {
		List<DemandeFileDTO> tmpFichiers = new ArrayList<DemandeFileDTO>();
		for (int i = 0; i < nombre; i++) {
			tmpFichiers.add(getRandomFile());
		}
		return tmpFichiers;
	}
	
	private static synchronized List<DemandeFileDTO> getFichiersSansDoublons(Integer nombre) {
		List<DemandeFileDTO> ret = new ArrayList<DemandeFileDTO>();
		for (int i = 0; i < nombre; i++) {
			boolean found = false;
			while (!found) {
				DemandeFileDTO randomFile = getRandomFile();
				if (fichiersDejaUtilisesPourDoublons.get(randomFile.getUrl()) == null) {
					fichiersDejaUtilisesPourDoublons.put(randomFile.getUrl(), randomFile);
					ret.add(randomFile);
					found = true;
				}
			}
		}
		return ret;
	}
	
	private static DemandeFileDTO getRandomFile() {
		int randomElementIndex = ThreadLocalRandom.current().nextInt(fichiers.size()) % fichiers.size();
		return fichiers.get(randomElementIndex);
	}
	
	private static String getRandomConfElement(String confElement) {
		String value = conf.get(confElement);
		String spl[] = value.split(",");
		int randomElementIndex = ThreadLocalRandom.current().nextInt(spl.length) % spl.length;
		return spl[randomElementIndex];
	}
	
	private static String getRandomContenu() {
		int randomElementIndex = ThreadLocalRandom.current().nextInt(contenus.size()) % contenus.size();
		return contenus.get(randomElementIndex);
	}
	
	private static void loadConf(String configFile) throws IOException {
		LOGGER.info("====== Chargement de la configuration...");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
			String line;
			while ((line = br.readLine()) != null) {
				String curToken = "";
				String key = null;
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(i) == '=') {
						key = curToken;
						curToken = "";
					}
					else {
						curToken += line.charAt(i);
					}
				}
				if ("contenu".equals(key)) {
					contenus.add(curToken);
				}
				else if ("fichier".equals(key)) {
			        DemandeFileDTO file = mapper.readValue(curToken, DemandeFileDTO.class);
			        fichiers.add(file);
				}
				else {
					conf.put(key, curToken);
				}
			}
		}
		LOGGER.info("API URL : {}", conf.get(CONFIG_APIURL));
		LOGGER.info("API JWT : {}", conf.get(CONFIG_APIJWT));
		LOGGER.info("buildid : {}", conf.get(CONFIG_BUILDID));
		LOGGER.info("canaux : {}", conf.get(CONFIG_CANAUX));
		LOGGER.info("langues : {}", conf.get(CONFIG_LANGUES));
		LOGGER.info("usagerids : {}", conf.get(CONFIG_USAGERIDS));
		LOGGER.info("Nombre de contenus : {}", contenus.size());
		LOGGER.info("Nombre de fichiers : {}", fichiers.size());
		LOGGER.info("=======\n");
	}
	
}
