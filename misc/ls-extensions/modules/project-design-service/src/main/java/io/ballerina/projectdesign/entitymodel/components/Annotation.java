package io.ballerina.projectdesign.entitymodel.components;

import java.util.List;
import java.util.Map;

public class Annotation {
    private final String type;
    private final Map<String, List<String>> fields;

    public Annotation(String type, Map<String, List<String>> fields) {
        this.type = type;
        this.fields = fields;
    }

    public String getType() {
        return type;
    }

    public Map<String, List<String>> getFields() {
        return fields;
    }
}
