package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.web.AssetManifestService;
import org.thymeleaf.processor.element.IElementTagStructureHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetAttributeTagProcessorTest {

    @Test
    void ensureDoProcessCallsAssetFilenameHashMapperAndSetsTheHashedValue() {
        final AssetManifestService assetManifestService = mock(AssetManifestService.class);
        final AssetAttributeTagProcessor assetAttributeTagProcessor = new AssetAttributeTagProcessor("uv", "href", assetManifestService);
        final String contextPath = "";

        when(assetManifestService.getHashedAssetFilename("awesome-attribute-value", contextPath))
            .thenReturn("value-with-hash");

        final IElementTagStructureHandler structureHandler = mock(IElementTagStructureHandler.class);
        assetAttributeTagProcessor.doProcess(null, null, null, "awesome-attribute-value", structureHandler);

        verify(structureHandler).setAttribute("href", "value-with-hash");
    }
}
