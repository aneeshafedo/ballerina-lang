package io.ballerina.multiservice.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Attribute {
    private String name;
    private String type;
    private boolean required;
    private boolean nillable;

    private String defaultValue;

    private List<Association> associations = new ArrayList<>(); // can have multiple association when union is found

    public Attribute(String name, String type, boolean required, boolean nillable, String defaultValue, List<Association> associations) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.nillable = nillable;
        this.defaultValue = defaultValue;
        this.associations = associations;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isNillable() {
        return nillable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }



    public static class Association {
        private String associate;
        private Cardinality cardinality;

        public Association(String associate, Cardinality cardinality) {
            this.associate = associate;
            this.cardinality = cardinality;
        }

        public Association() {

        }

        public void setAssociate(String associate) {
            this.associate = associate;
        }

        public void setCardinality(Cardinality cardinality) {
            this.cardinality = cardinality;
        }

        public static class Cardinality {
            private String self;
            private String associate;

            public Cardinality(String self, String associate) {
                this.self = self;
                this.associate = associate;
            }
        }
    }
}
