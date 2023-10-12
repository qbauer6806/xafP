package mc.gouv.sup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mc.gouv.xaf.shared.dto.es.GenericContenuDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.sup.model.ClassObject;
import mc.gouv.sup.model.Field;
import mc.gouv.sup.model.PackageObject;

public class GenerateGenericModel {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateGenericModel.class);

    /*
            Change TS_NAME and PATH_WORKSPACE
     */
    private static final String TS_NAME = "vvtcvlc";
    private static final long serialVersionUID = UUID.randomUUID().getMostSignificantBits();

    private static final String PATH_WORKSPACE = "D:\\Workspace\\";

    private static final String GENERIC_CLASS_PREFIX = TS_NAME.substring(0, 1).toUpperCase() + TS_NAME.substring(1) + "Generic";
    private static final String PATH = PATH_WORKSPACE + TS_NAME + "\\" + TS_NAME + "-shared\\src\\main\\java\\mc\\gouv\\" + TS_NAME + "\\shared\\model";
    private static final String GENERIC_NAME = "generic";
    private static final String MODEL_PACKAGE = "mc.gouv." + TS_NAME + ".shared.model";
    private static final String GENERIC_PACKAGE = MODEL_PACKAGE + "." + GENERIC_NAME;
    private static final String PATH_GENERIC = PATH + "/" + GENERIC_NAME;
    private static final String JAVA_EXTENSION = ".java";
    private static final String PUBLIC = "public";
    private static final String THIS = "this.";
    private static final String INDENT = "    ";
    private static final String EMPTY_METHOD_PARAM = "() {\n";
    private static final String END_OF_BLOCK = "}\n\n";
    private static final String FIELD_REGEX = "private ([a-zA-Z0-9]*\\[?\\]?) ([a-zA-Z0-9]*);";
    private static final Pattern PATTERN = Pattern.compile(FIELD_REGEX);
    private static final PackageObject PACKAGE_GENERIC = new PackageObject(GENERIC_NAME);
    private static final File GENERIC_DIRECTORY = new File(PATH_GENERIC);
    private static final List<String> WARNINGS = new ArrayList<>();
    private static final String CONTENU_PROJECT_DEMANDE_DTO = "ContenuProjectDemandeDTO";

    public static void main(String[] args) throws IOException {
        if (GENERIC_DIRECTORY.exists()) {
            FileUtils.deleteDirectory(GENERIC_DIRECTORY);
        }
        GENERIC_DIRECTORY.mkdirs();
        File file = new File(PATH);
        String[] pathnames = file.list();

        List<String> packages = new ArrayList<>(Arrays.asList(pathnames));

        List<PackageObject> packageObjects = new ArrayList<>(packages.size());

        for (String packageName : packages) {
            if (!packageName.equals(GENERIC_NAME)) {
                PackageObject packageObject = new PackageObject(packageName);
                packageObjects.add(packageObject);
                readClasses(packageObject);
            }
        }
        write(PACKAGE_GENERIC, packageObjects);

        LOGGER.info("{}", PACKAGE_GENERIC);
        LOGGER.info(" /!\\ WARNING /!\\");
        WARNINGS.forEach(LOGGER::info);
    }

    private static void write(PackageObject packageGeneric, List<PackageObject> packageObjects) throws IOException {
        for (ClassObject classObject : packageGeneric.getClassObjects()) {
            writeClass(classObject, packageObjects);
        }
    }

    private static void writeMembersVariables(BufferedWriter writer, ClassObject classObject) throws IOException {
        LOGGER.info("Écriture des Varibles Membres.");
        writer.append(INDENT).append("/* Members variables*/\n\n");
        String svuid = "" +serialVersionUID;
        writer.append(INDENT).append("private static final long serialVersionUID = ").append(svuid).append("L;\n\n");
        for (Field field : classObject.getFields()) {
            if (field.getType().contains("DTO") || field.getType().contains("Enum")) {
                writer.append(INDENT).append("private ").append(GENERIC_CLASS_PREFIX).append(field.getType()).append(' ').append(field.getName()).append(";\n");
            } else {
                writer.append(INDENT).append("private ").append(field.getType()).append(' ').append(field.getName()).append(";\n");
            }
        }
    }

    private static void writeConstructors(BufferedWriter writer, ClassObject classObject, List<PackageObject> packageObjects) throws IOException {
        LOGGER.info("Écriture des Constructeurs.");
        writer.append('\n').append(INDENT).append("/* Constructors*/\n\n");
        writer.append(INDENT).append(PUBLIC).append(' ').append(GENERIC_CLASS_PREFIX).append(classObject.getName()).append(EMPTY_METHOD_PARAM).append(INDENT).append(END_OF_BLOCK);
        for (PackageObject packageObject : packageObjects) {

            for (ClassObject classObjectFromPackage : packageObject.getClassObjects()) {
                if (classObject.getName().equals(classObjectFromPackage.getName())) {
                    writer.append(INDENT).append(PUBLIC).append(' ').append(GENERIC_CLASS_PREFIX).append(classObject.getName()).append('(').append(MODEL_PACKAGE).append('.').append(packageObject.getName()).append('.').append(classObject.getName()).append(" dto) {\n");
                    writeConstructorBlock(writer, classObjectFromPackage);
                    writer.append(INDENT).append(END_OF_BLOCK);
                }
            }
        }
    }

    private static void writeConstructorBlock(BufferedWriter writer, ClassObject classObjectFromPackage) throws IOException {
        for (Field field : classObjectFromPackage.getFields()) {
            String fieldNameWithUpper = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            if (field.getType().contains("DTO")) {
                if (field.getType().contains("[]")) {
                    String fieldStr = field.getType().replace("[]", "");
                    writer.append(INDENT).append(INDENT).append(THIS).append(field.getName()).append(" = Arrays.stream(dto.get").append(fieldNameWithUpper).append("()).map(").append(GENERIC_CLASS_PREFIX).append(fieldStr).append("::new).toArray(").append(GENERIC_CLASS_PREFIX).append(field.getType()).append("::new);\n");
                } else {
                    writer.append(INDENT).append(INDENT).append(THIS).append(field.getName()).append(" = new ").append(GENERIC_CLASS_PREFIX).append(field.getType()).append("(dto.get").append(fieldNameWithUpper).append("());\n");

                }
            } else if (field.getType().contains("Enum")) {
                writer.append(INDENT).append(INDENT).append("if (dto.get").append(fieldNameWithUpper).append("() != null) {\n");
                writer.append(INDENT).append(INDENT).append(" this.").append(field.getName()).append(" = ").append(GENERIC_CLASS_PREFIX).append(field.getType()).append(".forValue(dto.get").append(fieldNameWithUpper).append("().name());\n");
                writer.append(INDENT).append(INDENT).append("}\n");
            } else {
                writer.append(INDENT).append(INDENT).append(THIS).append(field.getName()).append(" = dto.get").append(fieldNameWithUpper).append("();\n");
            }
        }
    }

    private static void writeGettersAndSetters(BufferedWriter writer, ClassObject classObject) throws IOException {
        writer.append(INDENT).append("/* Getters and Setters*/\n\n");
        for (Field field : classObject.getFields()) {
            String fieldNameWithUpper = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
            if (field.getType().contains("DTO") || field.getType().contains("Enum")) {
                writer.append(INDENT).append(PUBLIC).append(' ').append(GENERIC_CLASS_PREFIX).append(field.getType()).append(" get").append(fieldNameWithUpper).append(EMPTY_METHOD_PARAM);
                writer.append(INDENT).append(INDENT).append("return ").append(field.getName()).append(";\n");
                writer.append(INDENT).append(END_OF_BLOCK);
                writer.append(INDENT).append(PUBLIC).append(" void set").append(fieldNameWithUpper).append('(').append(GENERIC_CLASS_PREFIX).append(field.getType()).append(" value) {\n");
            } else {
                writer.append(INDENT).append(PUBLIC).append(' ').append(field.getType()).append(" get").append(fieldNameWithUpper).append(EMPTY_METHOD_PARAM);
                writer.append(INDENT).append(INDENT).append("return ").append(field.getName()).append(";\n");
                writer.append(INDENT).append(END_OF_BLOCK);
                writer.append(INDENT).append(PUBLIC).append(" void set").append(fieldNameWithUpper).append('(').append(field.getType()).append(" value) {\n");
            }
            writer.append(INDENT).append(INDENT).append(THIS).append(field.getName()).append(" = value;\n");
            writer.append(INDENT).append(END_OF_BLOCK);
        }
    }

    private static void writeClass(ClassObject classObject, List<PackageObject> packageObjects) throws IOException {
        Set<String> fields = new HashSet<>();
        Set<String> duplicateFields = new HashSet<>();
        for (Field field : classObject.getFields()) {
            if (!fields.add(field.getName())) {
                duplicateFields.add(field.getName());
            }
        }

        if (!duplicateFields.isEmpty()) {
            WARNINGS.add("The class [" + classObject.getName() + "] contains some conflicts with fields : { " + duplicateFields.stream().collect(Collectors.joining(",")) + " }");
        }

        String className = classObject.getName();
        File file = new File(PATH_GENERIC + "/" + GENERIC_CLASS_PREFIX + className + JAVA_EXTENSION);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	        writer.append("package " + GENERIC_PACKAGE + "; \n\n");
	        writer.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n\n");
	        writer.append("import java.io.Serializable;\n");
	        if (classObject.getFields().stream().map(Field::getType).anyMatch(s -> s.contains("[]"))) {
	            writer.append("import java.util.Arrays;\n");
	        }
	
	        writer.append("\n@JsonIgnoreProperties(ignoreUnknown = true)\n");
	        writer.append(PUBLIC).append(" class ").append(GENERIC_CLASS_PREFIX).append(className);
            // On ajoute une extension du DTO Générique d'ElasticSearch si la classe en cours d'écriture est l'objet racine
            if (StringUtils.equals(CONTENU_PROJECT_DEMANDE_DTO, className)) {
                writer.append(" extends ").append(GenericContenuDTO.class.getName());
            }
            writer.append(" implements Serializable {\n\n");

	        writeMembersVariables(writer, classObject);

            writeConstructors(writer, classObject, packageObjects);
	
            writeGettersAndSetters(writer, classObject);

	        writer.append("}");
        }
    }

    private static void copyEnum(String enumName, String packageName) throws IOException {
        File file = new File(PATH_GENERIC + "/" + GENERIC_CLASS_PREFIX + enumName);
        File fileToCopy = new File(PATH + "/" + packageName + "/" + enumName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));


        try (Scanner scanner = new Scanner(fileToCopy)) {
            scanner.nextLine();
            writer.append("package " + GENERIC_PACKAGE + ";\n");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                line = line.replace(enumName.replace(JAVA_EXTENSION, ""), GENERIC_CLASS_PREFIX + enumName.replace(JAVA_EXTENSION, ""));
                writer.write(line + "\n");
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("FAIL file not found");
        }


        writer.close();
    }

    private static void readClasses(PackageObject packageObject) throws IOException {
        File f = new File(PATH + "/" + packageObject.getName());
        String[] pathnames = f.list();
        List<String> classes = new ArrayList<>(Arrays.asList(pathnames));
        for (String className : classes) {
            if (className.contains("Enum")) {
                copyEnum(className, packageObject.getName());
            } else {
                ClassObject classObject = new ClassObject(className.replace(JAVA_EXTENSION, ""));
                readFields(packageObject, classObject);
                packageObject.addClass(classObject);
            }
        }

    }

    private static void readFields(PackageObject packageObject, ClassObject classObject) {
        File file = new File(PATH + "/" + packageObject.getName() + "/" + classObject.getName() + JAVA_EXTENSION);

        ClassObject classObjecttoCreate = PACKAGE_GENERIC.getClassObjectOrCreateByName(classObject.getName());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.find()) {
                    classObject.addField(new Field(matcher.group(2), matcher.group(1)));
                    classObjecttoCreate.addField(new Field(matcher.group(2), matcher.group(1)));
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.info("FAIL file not found");
        }
    }


}