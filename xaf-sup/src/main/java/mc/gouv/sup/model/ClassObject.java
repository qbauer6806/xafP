package mc.gouv.sup.model;

import java.util.HashSet;
import java.util.Set;

public class ClassObject {
    private String name;
    private Set<Field> fields = new HashSet<>();

    public ClassObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public Set<Field> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("  ****************************************************************\n  * ")
                .append(name)
                .append("\n  ****************************************************************\n");

        fields.forEach(result::append);
        return result.toString();
    }
}
