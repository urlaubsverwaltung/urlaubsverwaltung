package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.synyx.urlaubsverwaltung.web.AssetManifestService;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

class AssetAttributeTagProcessor extends AbstractAttributeTagProcessor {

    private static final int PRECEDENCE = 10000;

    private final String attributeName;
    private final AssetManifestService assetManifestService;

    AssetAttributeTagProcessor(String dialectPrefix, String attributeName, AssetManifestService assetManifestService) {
        super(TemplateMode.HTML, dialectPrefix, null, false, attributeName, true, PRECEDENCE,true);
        this.attributeName = attributeName;
        this.assetManifestService = assetManifestService;
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        final String hashedAssetFilename = assetManifestService.getHashedAssetFilename(attributeValue);
        structureHandler.setAttribute(this.attributeName, hashedAssetFilename);
    }
}
