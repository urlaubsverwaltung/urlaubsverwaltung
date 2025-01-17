package org.synyx.urlaubsverwaltung.extension.backup.restore;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("uv.backup.restore-configuration")
public record RestoreConfigurationProperties(
    @DefaultValue("false") boolean enabled,
    @DefaultValue("false") boolean restoreOnAppReady,
    @DefaultValue("filesystem") String restoreService,
    @DefaultValue("false") boolean dropData
) {
}
