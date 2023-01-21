package org.synyx.urlaubsverwaltung.extension;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "uv.extensions.enabled", havingValue = "true")
@EnableConfigurationProperties(ExtensionConfigurationProperties.class)
public class ExtensionConfiguration {
}
