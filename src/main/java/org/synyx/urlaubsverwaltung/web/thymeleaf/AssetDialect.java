package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.Set;

class AssetDialect extends AbstractProcessorDialect {

    private final AssetFilenameHashMapper assetFilenameHashMapper;

    AssetDialect(AssetFilenameHashMapper assetFilenameHashMapper) {
        super("Asset Dialect", "asset", 1000);
        this.assetFilenameHashMapper = assetFilenameHashMapper;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(
            new AssetAttributeTagProcessor(dialectPrefix, "href", assetFilenameHashMapper),
            new AssetAttributeTagProcessor(dialectPrefix, "src", assetFilenameHashMapper)
        );
    }
}
