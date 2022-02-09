package mc.gouv.sup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GenerateMapperTool {
    private static String WORKSPACE = "D:\\Workspace\\";
    private static String TSCODE = "insenco";
    private static String LAST_RECAPS = "v1631698370800";

    private static String PATH = WORKSPACE + TSCODE + "\\" + TSCODE + "-shared\\src\\main\\java\\mc\\gouv\\" + TSCODE + "\\shared\\model\\" + LAST_RECAPS;
    private static String PATH_FILE = WORKSPACE + TSCODE + "\\" + TSCODE + "-service\\src\\main\\java\\mc\\gouv\\" + TSCODE + "\\service\\impl";

    public static void main(String[] args) throws IOException {

        File f = new File(PATH);
        String[] pathnames = f.list();

        List<String> enumMixIns = new ArrayList<>();
        List<String> otherMixIns = new ArrayList<>();

        for (String pathname : pathnames) {
            if (pathname.contains("Enum")) {
                enumMixIns.add("        mapper.addMixIn(" + pathname.substring(0, pathname.indexOf(".")) + ".class, EnumMixIn.class);");
            } else {
                PaysNationaliteEnum paysNationaliteResult = checkClass(PATH + "/" + pathname);
                if (paysNationaliteResult.equals(PaysNationaliteEnum.NATIONALITE) || paysNationaliteResult.equals(PaysNationaliteEnum.BOTH)) {
                    otherMixIns.add("        mapper.addMixIn(" + pathname.substring(0, pathname.indexOf(".")) + ".class, NationaliteMixIn.class);");
                }
                if (paysNationaliteResult.equals(PaysNationaliteEnum.PAYS) || paysNationaliteResult.equals(PaysNationaliteEnum.BOTH)) {
                    otherMixIns.add("        mapper.addMixIn(" + pathname.substring(0, pathname.indexOf(".")) + ".class, PaysMixIn.class);");
                }


            }
        }
        File file = new File(PATH_FILE+"/"+ TSCODE.substring(0, 1).toUpperCase() + TSCODE.substring(1) + "IndexedDemandeJsonMapperConfig.java");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.append("package mc.gouv." + TSCODE + ".service.impl;\n" +
                "\n" +
                "import com.fasterxml.jackson.databind.DeserializationFeature;\n" +
                "import com.fasterxml.jackson.databind.ObjectMapper;\n" +
                "import com.fasterxml.jackson.databind.SerializationFeature;\n" +
                "import mc.gouv.xaf.back.mapping.EnumMixIn;\n" +
                "import mc.gouv.xaf.back.mapping.NationaliteMixIn;\n" +
                "import mc.gouv.xaf.back.mapping.PaysMixIn;\n" +
                "import mc.gouv.insenco.shared.model."+LAST_RECAPS+".*;\n" +
                "import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;\n" +
                "import org.springframework.beans.factory.config.AutowireCapableBeanFactory;\n" +
                "import org.springframework.context.annotation.Bean;\n" +
                "import org.springframework.context.annotation.Conditional;\n" +
                "import org.springframework.context.annotation.Configuration;\n" +
                "import org.springframework.http.converter.json.SpringHandlerInstantiator;\n" +
                "\n" +
                "@Configuration\n" +
                "@Conditional(IndexationEnabledCondition.class)\n" +
                "public class " + TSCODE.substring(0, 1).toUpperCase() + TSCODE.substring(1) + "IndexedDemandeJsonMapperConfig {\n" +
                "\n" +
                "    @Bean\n" +
                "    public SpringHandlerInstantiator handlerInstantiator(AutowireCapableBeanFactory beanFactory) {\n" +
                "        return new SpringHandlerInstantiator(beanFactory);\n" +
                "    }");

        writer.append("\n\n");
        writer.append("    @Bean\n" +
                "    public ObjectMapper objectMapper(SpringHandlerInstantiator handlerInstantiator) {\n" +
                "        ObjectMapper mapper = new ObjectMapper();\n" +
                "\n" +
                "        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);\n" +
                "        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);\n");
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
        writer.close();
    }

    private static PaysNationaliteEnum checkClass(String filePath) {
        File file = new File(filePath);

        boolean hasPays = false;
        boolean hasNationalite = false;

        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("private String nationalite;")) {
                    hasNationalite = true;
                } else if (line.contains("private String pays;")) {
                    hasPays = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("FAIL file not found");
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