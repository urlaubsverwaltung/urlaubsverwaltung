package org.synyx.urlaubsverwaltung.extension;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("uv.extensions")
public class ExtensionConfigurationProperties {
    /**
     * when enabled application will bootstrap
     * beans required running external extensions
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
