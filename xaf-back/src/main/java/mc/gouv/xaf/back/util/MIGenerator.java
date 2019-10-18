package mc.gouv.xaf.back.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;

public class MIGenerator {

    public static void main(String[] args) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException, Exception {

        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        Velocity.init();
        Context context = new VelocityContext();
        
        
        // À remplir
        String file = "D:/qdeme/MIGenerator/";
        context.put("TSCode", "CANDI");
        context.put("TSNomComplet", "Déposer une candidature spontanée aux emplois de l'administration monégasque");
        context.put("PortFront", "20760");
        context.put("SuffixeURLFront", "candidature-spontanee-fpe");
        context.put("NomResponsable", "Quentin DEMÉ");
        // Fin
        
        
        context.put("TSCodeLowerCase", ((String)context.get("TSCode")).toLowerCase());
        FileWriter output = new FileWriter(new File(file + "MICustom.html"));
        String intext = readFile(file + "MIGenerique.html", Charset.forName("UTF-8"));

        if (!Velocity.evaluate(context, output, "GenTS", intext)) {
            throw new Exception("Velocity.evaluate() n'a pas fonctionné.");
        }
        
    }
    
    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
