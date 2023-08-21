package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.synyx.urlaubsverwaltung.web.AssetManifestService;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.Set;

class AssetDialect extends AbstractProcessorDialect {

    private final AssetManifestService assetManifestService;

    AssetDialect(AssetManifestService assetManifestService) {
        super("Asset Dialect", "asset", 1000);
        this.assetManifestService = assetManifestService;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(
            new AssetAttributeTagProcessor(dialectPrefix, "src", assetManifestService)
        );
    }
}
