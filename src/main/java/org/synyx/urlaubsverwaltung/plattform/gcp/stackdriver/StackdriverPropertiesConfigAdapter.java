package org.synyx.urlaubsverwaltung.plattform.gcp.stackdriver;

import io.micrometer.stackdriver.StackdriverConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;

/**
 * Adapter to convert {@link StackdriverProperties} to an {@link StackdriverConfig}.
 */
public class StackdriverPropertiesConfigAdapter extends StepRegistryPropertiesConfigAdapter<StackdriverProperties> implements StackdriverConfig {

    public StackdriverPropertiesConfigAdapter(StackdriverProperties properties) {
        super(properties);
    }

    @Override
    public String projectId() {
        return get(StackdriverProperties::getProjectId, StackdriverConfig.super::projectId);
    }

    @Override
    public String resourceType() {
        return get(StackdriverProperties::getResourceType, StackdriverConfig.super::resourceType);
    }
}
