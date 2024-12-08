package org.synyx.urlaubsverwaltung.tenancy.configuration.single;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SingleTenantConfigurationProperties.class)
@ConditionalOnSingleTenantMode
public class SingleTenantConfiguration {
}
