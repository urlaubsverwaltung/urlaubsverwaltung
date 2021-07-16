package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

class AssetAttributeTagProcessor extends AbstractAttributeTagProcessor {

    private static final int PRECEDENCE = 10000;

    private final String attributeName;
    private final AssetFilenameHashMapper assetFilenameHashMapper;

    AssetAttributeTagProcessor(String dialectPrefix, String attributeName, AssetFilenameHashMapper assetFilenameHashMapper) {
        super(TemplateMode.HTML, dialectPrefix, null, false, attributeName, true, PRECEDENCE,true);
        this.attributeName = attributeName;
        this.assetFilenameHashMapper = assetFilenameHashMapper;
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        final String hashedAssetFilename = assetFilenameHashMapper.getHashedAssetFilename(attributeValue);
        structureHandler.setAttribute(this.attributeName, hashedAssetFilename);
    }
}
