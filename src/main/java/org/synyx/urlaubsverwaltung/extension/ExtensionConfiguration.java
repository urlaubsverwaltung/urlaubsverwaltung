package org.synyx.urlaubsverwaltung.extension;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnExtensionsEnabled
@EnableConfigurationProperties(ExtensionConfigurationProperties.class)
public class ExtensionConfiguration {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    @PostConstruct
    public void logThatExtensionsAreEnabled() {
        LOG.info(">> Extension mode is enabled (uv.extensions.enabled=true)");
    }
}
