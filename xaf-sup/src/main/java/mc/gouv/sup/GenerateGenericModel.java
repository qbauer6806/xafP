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

import org.apache.commons.io.FileUtils;
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
    private static final String INDENT = "    ";
    private static final String FIELD_REGEX = "private ([a-zA-Z0-9]*\\[?\\]?) ([a-zA-Z0-9]*);";
    private static final Pattern PATTERN = Pattern.compile(FIELD_REGEX);
    private static final PackageObject PACKAGE_GENERIC = new PackageObject(GENERIC_NAME);
    private static final File GENERIC_DIRECTORY = new File(PATH_GENERIC);
    private static final List<String> WARNINGS = new ArrayList<>();

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

        LOGGER.info(PACKAGE_GENERIC.toString());
        LOGGER.info(" /!\\ WARNING /!\\");
        WARNINGS.forEach(System.out::println);
    }

    private static void write(PackageObject packageGeneric, List<PackageObject> packageObjects) throws IOException {
        for (ClassObject classObject : packageGeneric.getClassObjects()) {
            writeClass(classObject, packageObjects);
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


        File file = new File(PATH_GENERIC + "/" + GENERIC_CLASS_PREFIX + classObject.getName() + JAVA_EXTENSION);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	        writer.append("package " + GENERIC_PACKAGE + "; \n\n");
	        writer.append("import com.fasterxml.jackson.annotation.JsonIgnoreProperties;\n\n");
	        writer.append("import java.io.Serializable;\n");
	        if (classObject.getFields().stream().map(Field::getType).anyMatch(s -> s.contains("[]"))) {
	            writer.append("import java.util.Arrays;\n");
	        }
	
	        writer.append("\n@JsonIgnoreProperties(ignoreUnknown = true)\n");
	        writer.append("public class " + GENERIC_CLASS_PREFIX + classObject.getName() + " implements Serializable {\n\n");
	
	        writer.append(INDENT + "/* Members variables*/\n\n");
	        writer.append(INDENT + "private static final long serialVersionUID = " + serialVersionUID + "L;\n\n");
	        for (Field field : classObject.getFields()) {
	            if (field.getType().contains("DTO") || field.getType().contains("Enum")) {
	                writer.append(INDENT + "private " + GENERIC_CLASS_PREFIX + field.getType() + " " + field.getName() + ";\n");
	            } else {
	                writer.append(INDENT + "private " + field.getType() + " " + field.getName() + ";\n");
	            }
	
	        }
	        writer.append("\n" + INDENT + "/* Constructors*/\n\n");
	        writer.append(INDENT + "public " + GENERIC_CLASS_PREFIX + classObject.getName() + "() {\n" + INDENT + "}\n\n");
	        for (PackageObject packageObject : packageObjects) {
	
	            for (ClassObject classObjectFromPackage : packageObject.getClassObjects()) {
	                if (classObject.getName().equals(classObjectFromPackage.getName())) {
	                    writer.append(INDENT + "public " + GENERIC_CLASS_PREFIX + classObject.getName() + "(" + MODEL_PACKAGE + "." + packageObject.getName() + "." + classObject.getName() + " dto) {\n");
	
	                    for (Field field : classObjectFromPackage.getFields()) {
	                        String fieldNameWithUpper = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	                        if (field.getType().contains("DTO")) {
	                            if (field.getType().contains("[]")) {
	                                writer.append(INDENT + INDENT + "this." + field.getName() + " = Arrays.stream(dto.get" + fieldNameWithUpper + "()).map(" + GENERIC_CLASS_PREFIX + field.getType().replace("[]", "") + "::new).toArray(" + GENERIC_CLASS_PREFIX + field.getType() + "::new);\n");
	                            } else {
	                                writer.append(INDENT + INDENT + "this." + field.getName() + " = new " + GENERIC_CLASS_PREFIX + field.getType() + "(dto.get" + fieldNameWithUpper + "());\n");
	
	                            }
	                        } else if (field.getType().contains("Enum")) {
	                            writer.append(INDENT + INDENT + "if (dto.get" + fieldNameWithUpper + "() != null) {\n");
	                            writer.append(INDENT + INDENT + " this." + field.getName() + " = " + GENERIC_CLASS_PREFIX + field.getType() + ".forValue(dto.get" + fieldNameWithUpper + "().name());\n");
	                            writer.append(INDENT + INDENT + "}\n");
	                        } else {
	                            writer.append(INDENT + INDENT + "this." + field.getName() + " = dto.get" + fieldNameWithUpper + "();\n");
	                        }
	
	                    }
	                    writer.append(INDENT + "}\n\n");
	                }
	            }
	        }
	
	        writer.append(INDENT + "/* Getters and Setters*/\n\n");
	        for (Field field : classObject.getFields()) {
	            String fieldNameWithUpper = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	            if (field.getType().contains("DTO") || field.getType().contains("Enum")) {
	                writer.append(INDENT + "public " + GENERIC_CLASS_PREFIX + field.getType() + " get" + fieldNameWithUpper + "() {\n");
	                writer.append(INDENT + INDENT + "return " + field.getName() + ";\n");
	                writer.append(INDENT + "}\n\n");
	                writer.append(INDENT + "public void set" + fieldNameWithUpper + "(" + GENERIC_CLASS_PREFIX + field.getType() + " value) {\n");
	                writer.append(INDENT + INDENT + "this." + field.getName() + " = value;\n");
	                writer.append(INDENT + "}\n\n");
	            } else {
	                writer.append(INDENT + "public " + field.getType() + " get" + fieldNameWithUpper + "() {\n");
	                writer.append(INDENT + INDENT + "return " + field.getName() + ";\n");
	                writer.append(INDENT + "}\n\n");
	                writer.append(INDENT + "public void set" + fieldNameWithUpper + "(" + field.getType() + " value) {\n");
	                writer.append(INDENT + INDENT + "this." + field.getName() + " = value;\n");
	                writer.append(INDENT + "}\n\n");
	            }
	        }
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