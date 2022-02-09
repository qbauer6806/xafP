package mc.gouv.sup.model;

import java.util.HashSet;
import java.util.Set;

public class PackageObject {
    private String name;
    private Set<ClassObject> classObjects = new HashSet<>();

    public PackageObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addClass(ClassObject classObject) {
        this.classObjects.add(classObject);
    }

    public Set<ClassObject> getClassObjects() {
        return classObjects;
    }

    public ClassObject getClassObjectOrCreateByName(String className) {
        for (ClassObject classObject : classObjects) {
            if (classObject.getName().equals(className)) {
                return classObject;
            }
        }
        ClassObject classObject = new ClassObject(className);
        this.classObjects.add(classObject);
        return classObject;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("------------------------------------------------------------------\n          Package <")
                .append(name)
                .append("> generated successfully\n------------------------------------------------------------------\n");

        for (ClassObject classObject : classObjects) {
            result.append(classObject);
        }
        return result.toString();
    }
}
