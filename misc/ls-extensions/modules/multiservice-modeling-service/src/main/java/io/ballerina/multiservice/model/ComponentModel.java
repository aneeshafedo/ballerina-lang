package io.ballerina.multiservice.model;

import io.ballerina.multiservice.model.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents intermediate model to represent multi-service projects.
 */
public class ComponentModel {
    private final PackageId packageId;
    private final List<Service> services;

    private final Map<String, Entity> entities;

    public ComponentModel(PackageId packageId, List<Service> services, Map<String, Entity> entities) {
        this.packageId = packageId;
        this.services = services;
        this.entities = entities;
    }

    public PackageId getPackageId() {
        return packageId;
    }

    public List<Service> getServices() {
        return services;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    /**
     * Represent current package information.
     */
    public static class PackageId {
        private final String name;
        private final String org;
        private final String version;

        public PackageId(String name, String org, String version) {
            this.name = name;
            this.org = org;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getOrg() {
            return org;
        }

        public String getVersion() {
            return version;
        }
    }
}
