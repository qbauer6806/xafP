package mc.gouv.sup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateMapperTool {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateMapperTool.class);
	
    private static final String WORKSPACE = "D:\\Workspace\\";
    private static final String TSCODE = "insenco";
    private static final String LAST_RECAPS = "generic";
    private static final String PATH = WORKSPACE + TSCODE + "\\" + TSCODE + "-shared\\src\\main\\java\\mc\\gouv\\" + TSCODE + "\\shared\\model\\" + LAST_RECAPS;
    private static final String PATH_FILE = WORKSPACE + TSCODE + "\\" + TSCODE + "-service\\src\\main\\java\\mc\\gouv\\" + TSCODE + "\\service\\impl";
    private static final String MAPPER_ADD_MIXIN = "        mapper.addMixIn(";

    public static void main(String[] args) throws IOException {

        File f = new File(PATH);
        String[] pathnames = f.list();

        List<String> enumMixIns = new ArrayList<>();
        List<String> otherMixIns = new ArrayList<>();

        for (String pathname : pathnames) {
            if (pathname.contains("Enum")) {
                enumMixIns.add(MAPPER_ADD_MIXIN + pathname.substring(0, pathname.indexOf(".")) + ".class, EnumMixIn.class);");
            } else {
                PaysNationaliteEnum paysNationaliteResult = checkClass(PATH + "/" + pathname);
                if (paysNationaliteResult.equals(PaysNationaliteEnum.NATIONALITE) || paysNationaliteResult.equals(PaysNationaliteEnum.BOTH)) {
                    otherMixIns.add(MAPPER_ADD_MIXIN + pathname.substring(0, pathname.indexOf(".")) + ".class, NationaliteMixIn.class);");
                }
                if (paysNationaliteResult.equals(PaysNationaliteEnum.PAYS) || paysNationaliteResult.equals(PaysNationaliteEnum.BOTH)) {
                    otherMixIns.add(MAPPER_ADD_MIXIN + pathname.substring(0, pathname.indexOf(".")) + ".class, PaysMixIn.class);");
                }


            }
        }
        File file = new File(PATH_FILE + "/" + TSCODE.substring(0, 1).toUpperCase() + TSCODE.substring(1) + "IndexedDemandeJsonMapperConfig.java");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	
	        writer.append("package mc.gouv.").append(TSCODE).append(".service.impl;\n")
                    .append("\n")
                    .append("import com.fasterxml.jackson.databind.ObjectMapper;\n")
                    .append("import mc.gouv.xaf.back.mapping.EnumMixIn;\n")
                    .append("import mc.gouv.xaf.back.mapping.NationaliteMixIn;\n")
                    .append("import mc.gouv.xaf.back.mapping.PaysMixIn;\n")
                    .append("import mc.gouv.").append(TSCODE).append(".shared.model.").append(LAST_RECAPS).append(".*;\n")
                    .append("import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;\n")
                    .append("import org.springframework.beans.factory.annotation.Autowired;\n")
                    .append("import org.springframework.beans.factory.config.AutowireCapableBeanFactory;\n")
                    .append("import org.springframework.context.annotation.Bean;\n")
                    .append("import org.springframework.context.annotation.Conditional;\n")
                    .append("import org.springframework.context.annotation.Configuration;\n")
                    .append("import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;\n")
                    .append("import org.springframework.http.converter.json.SpringHandlerInstantiator;\n")
                    .append("\n")
                    .append("@Configuration\n")
                    .append("@Conditional(IndexationEnabledCondition.class)\n")
                    .append("public class ").append(TSCODE.substring(0, 1).toUpperCase()).append(TSCODE.substring(1)).append("IndexedDemandeJsonMapperConfig {\n")
                    .append("\n")
                    .append("    @Autowired\n")
                    .append("    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;\n\n")
                    .append("    @Bean\n")
                    .append("    public SpringHandlerInstantiator handlerInstantiator(AutowireCapableBeanFactory beanFactory) {\n")
                    .append("        return new SpringHandlerInstantiator(beanFactory);\n")
                    .append("    }");
	
	        writer.append("\n\n");
	        writer.append("    @Bean\n" +
	                "    public ObjectMapper objectMapper(SpringHandlerInstantiator handlerInstantiator) {\n" +
	                "        ObjectMapper mapper = jackson2ObjectMapperBuilder.build();\n\n");
	        for (String enumMixIn : enumMixIns) {
	            writer.append(enumMixIn).append("\n");
	        }
	        writer.append("\n");
	        if (!otherMixIns.isEmpty()) {
	            writer.append("        mapper.setHandlerInstantiator(handlerInstantiator);\n");
	        }
	        for (String otherMixIn : otherMixIns) {
	            writer.append(otherMixIn).append("\n");
	        }
	        writer.append("\n        return mapper;\n" +
	                "    }\n " +
	                "}");
	        writer.append("\n\n");
        }
    }

    private static PaysNationaliteEnum checkClass(String filePath) {
        File file = new File(filePath);

        boolean hasPays = false;
        boolean hasNationalite = false;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("private String nationalite;")) {
                    hasNationalite = true;
                } else if (line.contains("private String pays;")) {
                    hasPays = true;
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("FAIL file not found");
        }
        if (hasNationalite) {
            if (hasPays) {
                return PaysNationaliteEnum.BOTH;
            }
            return PaysNationaliteEnum.NATIONALITE;
        } else if (hasPays) {
            return PaysNationaliteEnum.PAYS;
        }
        return PaysNationaliteEnum.NONE;
    }


    private enum PaysNationaliteEnum {
        NONE, PAYS, NATIONALITE, BOTH;
    }
}