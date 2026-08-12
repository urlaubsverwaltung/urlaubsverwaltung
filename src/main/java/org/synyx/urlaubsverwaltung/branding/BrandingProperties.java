package org.synyx.urlaubsverwaltung.branding;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Name of this application as it is shown to users - in the web ui, in outgoing mails,
 * in exported calendars and in the api documentation.
 */
@Validated
@ConfigurationProperties("uv.branding")
public record BrandingProperties(@NotEmpty String name) {
}
