package mc.gouv.xaf.xaf12batch;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CustomClassLoader extends ClassLoader {
    private Map<String, String> packageReplacementMap;

    public CustomClassLoader(ClassLoader parent, Map<String, String> packageReplacementMap) {
        super(parent);
        this.packageReplacementMap = packageReplacementMap;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Replace the package name if it exists in the map
        String originalName = name;
        for (Map.Entry<String, String> entry : packageReplacementMap.entrySet()) {
            if (name.startsWith(entry.getKey())) {
                // Replace the old package with the new one
                name = entry.getValue() + name.substring(entry.getKey().length());
                break;
            }
        }

        // Convert the class name to path
        String path = name.replace('.', '/') + ".class";
        InputStream inputStream = getParent().getResourceAsStream(path);

        if (inputStream == null) {
            throw new ClassNotFoundException("Class not found: " + originalName);
        }

        try {
            // Read the bytecode of the class
            byte[] classBytes = inputStream.readAllBytes();
            return defineClass(originalName, classBytes, 0, classBytes.length);
        } catch (Exception e) {
            throw new ClassNotFoundException("Failed to load class: " + originalName, e);
        }
    }

    public static void main(String[] args) {
        try {
            // Map the original package to the new package
            Map<String, String> packageReplacement = new HashMap<>();
            packageReplacement.put("mc.gouv.af.back.bpm.model", "new.package.name");

            // Create custom class loader
            CustomClassLoader classLoader = new CustomClassLoader(CustomClassLoader.class.getClassLoader(), packageReplacement);

            // Load the class with the original package name
            Class<?> clazz = classLoader.loadClass("mc.gouv.af.back.bpm.model.CommentaireInterneDTO");
            System.out.println("Loaded class: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
