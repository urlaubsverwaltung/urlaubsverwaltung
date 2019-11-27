package org.synyx.urlaubsverwaltung.person;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("uv.person")
public class PersonConfigurationProperties {

    /**
     * A user can be manipulated or not in the uv
     * manipulated means to create and edit a user
     * but not their permissions.
     */
    private boolean canBeManipulated = false;

    public boolean isCanBeManipulated() {
        return canBeManipulated;
    }

    public void setCanBeManipulated(boolean canBeManipulated) {
        this.canBeManipulated = canBeManipulated;
    }
}
