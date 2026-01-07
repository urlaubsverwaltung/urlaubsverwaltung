package org.synyx.urlaubsverwaltung.web.headerscript;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("uv.header-script")
public record HeaderScriptConfigProperties(
    @DefaultValue("false") boolean enabled,
    @NotNull @DefaultValue("") String content
) {
}
