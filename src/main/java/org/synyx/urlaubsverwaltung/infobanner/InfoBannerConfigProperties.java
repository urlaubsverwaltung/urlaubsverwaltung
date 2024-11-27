package org.synyx.urlaubsverwaltung.infobanner;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("uv.info-banner")
public record InfoBannerConfigProperties(boolean enabled, @NotNull @Valid Text text) {

    public record Text(@NotEmpty String de) {
    }
}
