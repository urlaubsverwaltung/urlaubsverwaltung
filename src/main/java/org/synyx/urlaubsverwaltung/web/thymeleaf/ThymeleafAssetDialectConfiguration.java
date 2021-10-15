package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
class ThymeleafAssetDialectConfiguration {

    @Bean
    AssetDialect assetDialect(ResourceLoader resourceLoader) {
        return new AssetDialect(new AssetFilenameHashMapper(resourceLoader));
    }
}
