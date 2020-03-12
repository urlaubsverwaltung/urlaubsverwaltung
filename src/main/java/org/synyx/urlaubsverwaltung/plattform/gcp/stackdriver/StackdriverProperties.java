package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "management.metrics.export.stackdriver")
public class StackdriverProperties extends StepRegistryProperties {

    /**
     * The ID of your google cloud plattform project
     */
    private String projectId;

    private String resourceType = "global";

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
