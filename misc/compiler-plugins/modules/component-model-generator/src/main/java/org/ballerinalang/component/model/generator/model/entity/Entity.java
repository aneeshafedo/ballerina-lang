package org.ballerinalang.component.model.generator.model.entity;

import java.util.List;

public class Entity {
    private List<Attribute> attributes;

    public Entity(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
