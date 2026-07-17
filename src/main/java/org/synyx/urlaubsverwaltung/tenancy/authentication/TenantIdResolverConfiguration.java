package org.synyx.urlaubsverwaltung.tenancy.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class TenantIdResolverConfiguration {

    @Bean
    TenantIdProvider tenantIdProvider(List<TenantIdResolver> resolvers) {
        return new TenantIdProvider(resolvers);
    }
}
