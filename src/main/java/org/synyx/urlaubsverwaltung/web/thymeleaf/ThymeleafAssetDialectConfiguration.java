package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.synyx.urlaubsverwaltung.web.AssetManifestService;

@Configuration
class ThymeleafAssetDialectConfiguration {

    @Bean
    AssetDialect assetDialect(AssetManifestService assetManifestService) {
        return new AssetDialect(assetManifestService);
    }
}
